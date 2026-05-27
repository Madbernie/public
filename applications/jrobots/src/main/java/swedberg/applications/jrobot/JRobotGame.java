package swedberg.applications.jrobot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class JRobotGame {
    static final double OBSTACLE_MARGIN = 60;

    private final List<JRobot> robots = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Grenade> grenades = new ArrayList<>();
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final List<Heart> hearts = new ArrayList<>();
    private final Random random = new Random();
    private int arenaWidth = 1200;
    private int arenaHeight = 900;
    private boolean gameOver;
    private String winner;
    private int gameOverTimer;
    private int tickCount;
    private int heartSpawnTimer;
    private final long startTime = System.currentTimeMillis();
    private static int gameNumber;
    private boolean obstaclesGenerated;

    public static Map<String, Integer> getWinCounts() { return WinCountStore.getCounts(); }

    public void ensureObstaclesGenerated() {
        if (obstaclesGenerated) return;
        generateObstacles();
        obstaclesGenerated = true;
    }

    public JRobotGame() {
        RobotController ai = new RobotAI("Krossarn", RobotAI.Strategy.AGGRESSIVE);
        RobotController user = new RobotUser();
        robots.add(createRobot(ai, new java.awt.Color(180, 60, 180)));
        robots.get(robots.size() - 1).setPattern(RobotPattern.TARGET);
        robots.add(createRobot(user, new java.awt.Color(60, 100, 220)));
        robots.get(robots.size() - 1).setPattern(RobotPattern.STRIPES);
        robots.add(createRobot(new RobotAI("Prickskyttarn", RobotAI.Strategy.SNIPER),
            new java.awt.Color(60, 200, 60)));
        robots.get(robots.size() - 1).setPattern(RobotPattern.ZEBRA);
        robots.add(createRobot(new RobotAI("Spöket", RobotAI.Strategy.EVASIVE),
            new java.awt.Color(220, 220, 40)));
        robots.get(robots.size() - 1).setPattern(RobotPattern.POLKA);
        gameNumber++;
        GameLogger.log(String.format("Spel %d startar — %s",
            gameNumber, java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"))));
    }

    private JRobot createRobot(RobotController controller, java.awt.Color color) {
        double x, y;
        double dir = random.nextDouble() * 2 * Math.PI;
        x = JRobot.RADIUS + random.nextDouble() * (arenaWidth - 2 * JRobot.RADIUS);
        y = JRobot.RADIUS + random.nextDouble() * (arenaHeight - 2 * JRobot.RADIUS);
        for (JRobot existing : robots) {
            while (Math.hypot(x - existing.getX(), y - existing.getY()) < 300) {
                x = JRobot.RADIUS + random.nextDouble() * (arenaWidth - 2 * JRobot.RADIUS);
                y = JRobot.RADIUS + random.nextDouble() * (arenaHeight - 2 * JRobot.RADIUS);
            }
        }
        return new JRobot(x, y, dir, color, controller);
    }

    private void generateObstacles() {
        int count = 10 + random.nextInt(6);
        for (int i = 0; i < count; i++) {
            double size = 30 + random.nextDouble() * 10;
            for (int attempt = 0; attempt < 200; attempt++) {
                double x = 50 + random.nextDouble() * (arenaWidth - size - 100);
                double y = 50 + random.nextDouble() * (arenaHeight - size - 100);
                if (tooCloseToRobots(x, y, size) || overlapsObstacles(x, y, size))
                    continue;
                obstacles.add(new Obstacle(x, y, size));
                break;
            }
        }
    }

    private boolean tooCloseToRobots(double ox, double oy, double size) {
        double cx = ox + size / 2;
        double cy = oy + size / 2;
        for (JRobot r : robots) {
            if (Math.hypot(r.getX() - cx, r.getY() - cy) < OBSTACLE_MARGIN)
                return true;
        }
        return false;
    }

    private boolean overlapsObstacles(double x, double y, double s) {
        for (Obstacle o : obstacles) {
            if (x < o.getX() + o.getSize() && x + s > o.getX()
                && y < o.getY() + o.getSize() && y + s > o.getY())
                return true;
        }
        return false;
    }

    public void update() {
        if (gameOver) { gameOverTimer--; return; }
        updateProjectiles();
        updateGrenades();
        updateHearts();
        for (JRobot robot : robots) {
            robot.ageEstimatedTarget();
            robot.tickCooldowns();
        }
        for (JRobot robot : robots) {
            if (robot.isAlive()) robot.tick(this);
        }
        for (JRobot robot : robots) {
            if (robot.isAlive()) robot.move(arenaWidth, arenaHeight);
        }
        resolveCollisions();
        checkHeartCollection();
        checkGameOver();
        tickCount++;
        if (tickCount % 25 == 0) logDebug();
    }

    private void updateHearts() {
        if (--heartSpawnTimer > 0) return;
        heartSpawnTimer = Heart.SPAWN_INTERVAL;
        hearts.removeIf(h -> !h.isAlive());
        if (!hearts.isEmpty()) return;
        for (int attempt = 0; attempt < 100; attempt++) {
            double hx = Heart.RADIUS + random.nextDouble()
                * (arenaWidth - 2 * Heart.RADIUS);
            double hy = Heart.RADIUS + random.nextDouble()
                * (arenaHeight - 2 * Heart.RADIUS);
            if (heartOverlapsObstacle(hx, hy)) continue;
            hearts.add(new Heart(hx, hy));
            return;
        }
    }

    private boolean heartOverlapsObstacle(double hx, double hy) {
        for (Obstacle o : obstacles) {
            double cx = Math.max(o.getX(), Math.min(hx, o.getX() + o.getSize()));
            double cy = Math.max(o.getY(), Math.min(hy, o.getY() + o.getSize()));
            if (Math.hypot(hx - cx, hy - cy) < Heart.RADIUS + 2) return true;
        }
        return false;
    }

    private void checkHeartCollection() {
        for (JRobot robot : robots) {
            if (!robot.isAlive()) continue;
            for (Heart h : hearts) {
                if (!h.isAlive()) continue;
                double dx = robot.getX() - h.getX();
                double dy = robot.getY() - h.getY();
                if (Math.hypot(dx, dy) < JRobot.RADIUS + Heart.RADIUS) {
                    h.collect();
                    robot.heal(Heart.HEAL_AMOUNT);
                }
            }
        }
    }

    private void updateProjectiles() {
        for (Projectile p : projectiles) p.move(arenaWidth, arenaHeight);
        projectiles.removeIf(p -> !p.isAlive());
        for (Projectile p : projectiles) {
            boolean hitObstacle = false;
            for (Obstacle o : obstacles) {
                if (p.getX() >= o.getX() && p.getX() <= o.getX() + o.getSize()
                    && p.getY() >= o.getY() && p.getY() <= o.getY() + o.getSize()) {
                    p.destroy();
                    hitObstacle = true;
                    break;
                }
            }
            if (hitObstacle) continue;
            for (JRobot robot : robots) {
                if (!robot.isAlive()) continue;
                double dx = p.getX() - robot.getX();
                double dy = p.getY() - robot.getY();
                if (dx * dx + dy * dy < (Projectile.RADIUS + JRobot.RADIUS)
                    * (Projectile.RADIUS + JRobot.RADIUS)) {
                    robot.takeDamage(5);
                    p.destroy();
                }
            }
        }
        projectiles.removeIf(p -> !p.isAlive());
    }

    private void updateGrenades() {
        for (Grenade g : grenades) g.move();
        checkGrenadeCollisions();
        applyBlastDamage();
        grenades.removeIf(g -> !g.isAlive());
    }

    private void checkGrenadeCollisions() {
        for (Grenade g : grenades) {
            if (!g.isInFlight()) continue;
            boolean hitObstacle = false;
            for (Obstacle o : obstacles) {
                if (g.getX() >= o.getX() && g.getX() <= o.getX() + o.getSize()
                    && g.getY() >= o.getY() && g.getY() <= o.getY() + o.getSize()) {
                    g.explode();
                    hitObstacle = true;
                    break;
                }
            }
            if (hitObstacle) continue;
            for (JRobot robot : robots) {
                if (!robot.isAlive()) continue;
                double dx = g.getX() - robot.getX();
                double dy = g.getY() - robot.getY();
                if (dx * dx + dy * dy < (Grenade.RADIUS + JRobot.RADIUS)
                    * (Grenade.RADIUS + JRobot.RADIUS)) {
                    g.explode();
                    break;
                }
            }
        }
    }

    private void applyBlastDamage() {
        for (Grenade g : grenades) {
            if (!g.isExploded()) continue;
            for (JRobot robot : robots) {
                if (!robot.isAlive()) continue;
                double dx = robot.getX() - g.getBlastCenterX();
                double dy = robot.getY() - g.getBlastCenterY();
                if (Math.hypot(dx, dy) < g.getBlastRadius()) {
                    robot.takeDamage(Grenade.DAMAGE);
                }
            }
        }
    }

    private void logDebug() {
        System.out.println("--- Tick " + tickCount + " ---");
        for (JRobot r : robots) {
            String name = r.getController().getName();
            System.out.printf("%s: (%.0f, %.0f) dir=%.0f\u00B0 spd=%.0f HP=%.0f%%",
                name, r.getX(), r.getY(),
                Math.toDegrees(r.getDirection()), r.getSpeed(), r.getHealth());
            if (r.hasEstimatedTarget()) {
                double dx = r.getEstimatedTargetX() - r.getX();
                double dy = r.getEstimatedTargetY() - r.getY();
                double dist = Math.hypot(dx, dy);
                double angle = Math.toDegrees(Math.atan2(dy, dx));
                System.out.printf(" | m\u00e5l: (%.0f, %.0f) dist=%.0f vinkel=%.0f\u00B0",
                    r.getEstimatedTargetX(), r.getEstimatedTargetY(), dist, angle);
            }
            System.out.println();
        }
    }

    private void checkGameOver() {
        List<JRobot> alive = new ArrayList<>();
        for (JRobot robot : robots) {
            if (robot.isAlive()) alive.add(robot);
        }
        if (alive.size() <= 1) {
            gameOver = true;
            gameOverTimer = 180;
            long elapsed = System.currentTimeMillis() - startTime;
            GameLogger.log(String.format("%s — %s — %d ms (%d ticks)",
                java.time.LocalTime.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")),
                alive.size() == 1 ? alive.get(0).getController().getName() : "Ingen",
                elapsed, tickCount));
            for (JRobot robot : robots) {
                robot.notifyGameOver(robot.isAlive());
            }
            if (alive.size() == 1) {
                winner = alive.get(0).getController().getName();
                WinCountStore.increment(winner);
            } else {
                winner = "Ingen";
            }
        }
    }

    public boolean isGameOver() { return gameOver; }
    public String getWinner() { return winner; }
    public int getGameOverTimer() { return gameOverTimer; }
    public int getAliveRobotCount() {
        int count = 0;
        for (JRobot r : robots) { if (r.isAlive()) count++; }
        return count;
    }
    public List<JRobot> getRobots() { return robots; }
    public List<Projectile> getProjectiles() { return projectiles; }

    public int getArenaWidth() { return arenaWidth; }
    public int getArenaHeight() { return arenaHeight; }

    public void setArenaSize(int width, int height) {
        this.arenaWidth = width;
        this.arenaHeight = height;
    }

    double scan(JRobot asker, double directionRad) {
        double cos = Math.cos(directionRad);
        double sin = Math.sin(directionRad);
        double bestDistance = Double.MAX_VALUE;
        JRobot hitRobot = findClosestRobot(asker, cos, sin);
        boolean hitHeart = false;

        if (hitRobot != null) {
            double dx = hitRobot.getX() - asker.getX();
            double dy = hitRobot.getY() - asker.getY();
            double dot = dx * cos + dy * sin;
            double cross = Math.abs(dx * sin - dy * cos);
            bestDistance = dot - Math.sqrt(JRobot.RADIUS * JRobot.RADIUS - cross * cross);
        }

        for (Heart heart : hearts) {
            if (!heart.isAlive()) continue;
            double t = rayCircleDist(asker.getX(), asker.getY(), cos, sin,
                heart.getX(), heart.getY(), Heart.RADIUS);
            if (t < bestDistance) { bestDistance = t; hitRobot = null; hitHeart = true; }
        }

        for (Obstacle obs : obstacles) {
            double t = rayRectDist(asker.getX(), asker.getY(), cos, sin,
                obs.getX(), obs.getY(), obs.getSize(), obs.getSize());
            if (t < bestDistance) { bestDistance = t; hitRobot = null; hitHeart = false; }
        }

        double wallDist = rayRectDist(asker.getX(), asker.getY(), cos, sin,
            0, 0, arenaWidth, arenaHeight);
        if (wallDist < bestDistance) { bestDistance = wallDist; hitRobot = null; hitHeart = false; }

        return recordScanResult(asker, hitRobot, hitHeart, bestDistance, wallDist);
    }

    private double recordScanResult(JRobot asker, JRobot hitRobot,
        boolean hitHeart, double best, double wallDist) {
        if (hitRobot != null) {
            asker.setLastScanHitDistance(best);
            asker.setLastScanHitRobotName(hitRobot.getController().getName());
            asker.setLastScanHitType(ScanHitType.ROBOT);
            return best;
        }
        if (hitHeart && best < wallDist - 1) {
            asker.setLastScanHitDistance(best);
            asker.setLastScanHitRobotName(null);
            asker.setLastScanHitType(ScanHitType.HEART);
            return best;
        }
        if (best < Double.MAX_VALUE && best < wallDist - 1) {
            asker.setLastScanHitDistance(best);
            asker.setLastScanHitRobotName(null);
            asker.setLastScanHitType(ScanHitType.OBSTACLE);
            return best;
        }
        if (wallDist < Double.MAX_VALUE) {
            asker.setLastScanHitDistance(wallDist);
            asker.setLastScanHitRobotName(null);
            asker.setLastScanHitType(ScanHitType.WALL);
            return wallDist;
        }
        asker.setLastScanHitDistance(-1);
        asker.setLastScanHitRobotName(null);
        asker.setLastScanHitType(ScanHitType.NOTHING);
        return -1;
    }

    private void resolveCollisions() {
        resolveRobotCollisions();
        resolveObstacleCollisions();
    }

    private void resolveRobotCollisions() {
        for (int i = 0; i < robots.size(); i++) {
            JRobot a = robots.get(i);
            if (!a.isAlive()) continue;
            for (int j = i + 1; j < robots.size(); j++) {
                JRobot b = robots.get(j);
                if (!b.isAlive()) continue;
                double dx = b.getX() - a.getX();
                double dy = b.getY() - a.getY();
                double dist = Math.hypot(dx, dy);
                double minDist = JRobot.RADIUS * 2;
                if (dist >= minDist || dist < 0.01) continue;
                double nx = dx / dist;
                double ny = dy / dist;
                double overlap = minDist - dist;
                a.setPosition(a.getX() - nx * overlap / 2, a.getY() - ny * overlap / 2);
                b.setPosition(b.getX() + nx * overlap / 2, b.getY() + ny * overlap / 2);
                a.bounce(-nx, -ny);
                b.bounce(nx, ny);
                a.applyCollisionDamage(5);
                b.applyCollisionDamage(5);
            }
        }
    }

    private void resolveObstacleCollisions() {
        for (JRobot robot : robots) {
            if (!robot.isAlive()) continue;
            for (Obstacle o : obstacles) {
                double cx = Math.max(o.getX(), Math.min(robot.getX(), o.getX() + o.getSize()));
                double cy = Math.max(o.getY(), Math.min(robot.getY(), o.getY() + o.getSize()));
                double dx = robot.getX() - cx;
                double dy = robot.getY() - cy;
                double dist = Math.hypot(dx, dy);
                if (dist >= JRobot.RADIUS || dist < 0.01) continue;
                double nx = dx / dist;
                double ny = dy / dist;
                double overlap = JRobot.RADIUS - dist;
                robot.setPosition(robot.getX() + nx * overlap, robot.getY() + ny * overlap);
                robot.bounce(nx, ny);
                robot.applyCollisionDamage(1);
            }
        }
    }

    private JRobot findClosestRobot(JRobot asker, double cos, double sin) {
        JRobot closest = null;
        double best = Double.MAX_VALUE;
        for (JRobot other : robots) {
            if (other == asker || !other.isAlive()) continue;
            double dx = other.getX() - asker.getX();
            double dy = other.getY() - asker.getY();
            double dot = dx * cos + dy * sin;
            if (dot < 0) continue;
            double cross = Math.abs(dx * sin - dy * cos);
            if (cross > JRobot.RADIUS) continue;
            double t = dot - Math.sqrt(JRobot.RADIUS * JRobot.RADIUS - cross * cross);
            if (t < best) { best = t; closest = other; }
        }
        return closest;
    }

    private double rayRectDist(double ox, double oy, double dx, double dy,
        double rx, double ry, double rw, double rh) {
        double tmin = -Double.MAX_VALUE;
        double tmax = Double.MAX_VALUE;

        if (Math.abs(dx) < 1e-10) {
            if (ox < rx || ox > rx + rw) return Double.MAX_VALUE;
        } else {
            double t1 = (rx - ox) / dx;
            double t2 = (rx + rw - ox) / dx;
            if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
            if (t1 > tmin) tmin = t1;
            if (t2 < tmax) tmax = t2;
            if (tmin > tmax || tmax < 0) return Double.MAX_VALUE;
        }

        if (Math.abs(dy) < 1e-10) {
            if (oy < ry || oy > ry + rh) return Double.MAX_VALUE;
        } else {
            double t1 = (ry - oy) / dy;
            double t2 = (ry + rh - oy) / dy;
            if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }
            if (t1 > tmin) tmin = t1;
            if (t2 < tmax) tmax = t2;
            if (tmin > tmax || tmax < 0) return Double.MAX_VALUE;
        }

        return tmin > 0 ? tmin : tmax;
    }

    private double rayCircleDist(double ox, double oy, double dx, double dy,
        double cx, double cy, double r) {
        double ocx = cx - ox;
        double ocy = cy - oy;
        double tca = ocx * dx + ocy * dy;
        if (tca < 0) return Double.MAX_VALUE;
        double d2 = ocx * ocx + ocy * ocy - tca * tca;
        if (d2 > r * r) return Double.MAX_VALUE;
        return tca - Math.sqrt(r * r - d2);
    }

    void addProjectile(Projectile p) { projectiles.add(p); }
    void addGrenade(Grenade g) { grenades.add(g); }

    public Grenade[] getGrenades() {
        return grenades.toArray(new Grenade[0]);
    }

    public Obstacle[] getObstacles() {
        return obstacles.toArray(new Obstacle[0]);
    }

    public Heart[] getHearts() {
        return hearts.toArray(new Heart[0]);
    }
}
