package swedberg.applications.jrobot;

import java.awt.Color;

public class JRobot {
    static final double RADIUS = 18;
    static final double MAX_SPEED = 20.0;
    static final double TURN_STEP = 5.0;
    static final int SCAN_COOLDOWN = 1;
    static final int SHOOT_COOLDOWN = 20;
    static final int GRENADE_COOLDOWN = 50;
    static final int SCAN_VISUAL_TICKS = 18;

    private double x;
    private double y;
    private double direction;
    private double speed;
    private double health = 150;
    private final Color color;
    private final RobotController controller;
    private RobotPattern pattern;

    private int scanCooldown;
    private int shootCooldown;
    private int grenadeCooldown;
    private double lastScanDirection;
    private double lastScanResult = -2;
    private double lastScanHitDistance = -1;
    private String lastScanHitRobotName;
    private int scanVisualTimer;
    private ScanHitType lastScanHitType = ScanHitType.COOLDOWN;
    private double estimatedTargetX;
    private double estimatedTargetY;
    private boolean hasEstimatedTarget;
    private int estimatedTargetAge;
    private int collisionDamageCooldown;

    public JRobot(double x, double y, double direction, Color color, RobotController controller) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.color = color;
        this.controller = controller;
    }

    public void tick(JRobotGame game) {
        GameAPI api = new GameAPIImpl(this, game);
        controller.tick(api);
    }

    public void move(int arenaWidth, int arenaHeight) {
        x += Math.cos(direction) * speed;
        y += Math.sin(direction) * speed;
        if (x < RADIUS) { x = RADIUS; direction = Math.PI - direction; wallHit(); }
        if (x > arenaWidth - RADIUS) { x = arenaWidth - RADIUS; direction = Math.PI - direction; wallHit(); }
        if (y < RADIUS) { y = RADIUS; direction = -direction; wallHit(); }
        if (y > arenaHeight - RADIUS) { y = arenaHeight - RADIUS; direction = -direction; wallHit(); }
    }

    private void wallHit() {
        if (speed > 0 && collisionDamageCooldown <= 0) {
            takeDamage(1);
            collisionDamageCooldown = 20;
        }
    }

    void setPosition(double px, double py) { x = px; y = py; }

    void bounce(double nx, double ny) {
        double cos = Math.cos(direction);
        double sin = Math.sin(direction);
        double dot = cos * nx + sin * ny;
        if (dot < 0) {
            direction = Math.atan2(sin - 2 * dot * ny, cos - 2 * dot * nx);
        }
    }

    public void tickCooldowns() {
        if (scanCooldown > 0) scanCooldown--;
        if (shootCooldown > 0) shootCooldown--;
        if (grenadeCooldown > 0) grenadeCooldown--;
        if (scanVisualTimer > 0) scanVisualTimer--;
        if (collisionDamageCooldown > 0) collisionDamageCooldown--;
    }

    public void takeDamage(double amount) {
        health = Math.max(0, health - amount);
    }

    public void heal(double amount) {
        health = Math.min(150, health + amount);
    }

    public boolean isAlive() { return health > 0; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getDirection() { return direction; }
    public double getSpeed() { return speed; }
    public double getHealth() { return health; }
    public Color getColor() { return color; }
    public RobotController getController() { return controller; }
    public RobotPattern getPattern() { return pattern; }
    void setPattern(RobotPattern pattern) { this.pattern = pattern; }
    void notifyGameOver(boolean won) { controller.onGameOver(won, health); }
    public double getLastScanDirection() { return lastScanDirection; }
    public double getLastScanResult() { return lastScanResult; }
    public double getLastScanHitDistance() { return lastScanHitDistance; }
    public String getLastScanHitRobotName() { return lastScanHitRobotName; }
    public ScanHitType getLastScanHitType() { return lastScanHitType; }
    public int getScanVisualTimer() { return scanVisualTimer; }
    public boolean canScan() { return scanCooldown <= 0; }
    public boolean canShoot() { return shootCooldown <= 0; }
    public boolean canThrowGrenade() { return grenadeCooldown <= 0; }

    public void incSpeed() { speed = Math.min(speed + 1, MAX_SPEED); }
    public void decSpeed() { speed = Math.max(speed - 1, 0); }
    public void turnClockwise() { direction += Math.toRadians(TURN_STEP); }
    public void turnCounterClockwise() { direction -= Math.toRadians(TURN_STEP); }

    void setScanCooldown(int cooldown) { scanCooldown = cooldown; }
    void setShootCooldown(int cooldown) { shootCooldown = cooldown; }
    void setLastScanDirection(double dir) { lastScanDirection = dir; }
    void setLastScanResult(double result) { lastScanResult = result; }
    void setLastScanHitDistance(double d) { lastScanHitDistance = d; }
    void setLastScanHitRobotName(String name) { lastScanHitRobotName = name; }
    void setLastScanHitType(ScanHitType type) { lastScanHitType = type; }
    void setScanVisualTimer(int timer) { scanVisualTimer = timer; }

    public void setEstimatedTarget(double x, double y) {
        estimatedTargetX = x;
        estimatedTargetY = y;
        hasEstimatedTarget = true;
        estimatedTargetAge = 0;
    }

    public void ageEstimatedTarget() {
        if (!hasEstimatedTarget) return;
        estimatedTargetAge++;
        if (estimatedTargetAge > 90) hasEstimatedTarget = false;
    }

    public boolean hasEstimatedTarget() { return hasEstimatedTarget; }
    public double getEstimatedTargetX() { return estimatedTargetX; }
    public double getEstimatedTargetY() { return estimatedTargetY; }
    public int getEstimatedTargetAge() { return estimatedTargetAge; }
    void clearEstimatedTarget() { hasEstimatedTarget = false; }

    boolean applyCollisionDamage(double amount) {
        if (collisionDamageCooldown > 0) return false;
        takeDamage(amount);
        collisionDamageCooldown = 20;
        return true;
    }

    private static class GameAPIImpl implements GameAPI {
        private final JRobot robot;
        private final JRobotGame game;

        GameAPIImpl(JRobot robot, JRobotGame game) {
            this.robot = robot;
            this.game = game;
        }

        @Override
        public double scan(double directionDegrees) {
            if (!robot.canScan()) {
                robot.setLastScanHitType(ScanHitType.COOLDOWN);
                return -2;
            }
            double dirRad = Math.toRadians(directionDegrees);
            robot.setScanCooldown(SCAN_COOLDOWN);
            robot.setScanVisualTimer(SCAN_VISUAL_TICKS);
            robot.setLastScanDirection(dirRad);
            double result = game.scan(robot, dirRad);
            robot.setLastScanResult(result);
            if (result >= 0 && robot.getLastScanHitType() == ScanHitType.ROBOT) {
                robot.setEstimatedTarget(
                    robot.getX() + Math.cos(dirRad) * result,
                    robot.getY() + Math.sin(dirRad) * result);
            }
            return result;
        }

        @Override
        public boolean shoot() {
            if (!robot.canShoot()) return false;
            robot.setShootCooldown(SHOOT_COOLDOWN);
            game.addProjectile(new Projectile(
                robot.getX() + Math.cos(robot.getDirection()) * RADIUS,
                robot.getY() + Math.sin(robot.getDirection()) * RADIUS,
                robot.getDirection(), robot.getColor()));
            return true;
        }

        @Override
        public boolean throwGrenade(double directionDegrees) {
            if (!robot.canThrowGrenade()) return false;
            robot.grenadeCooldown = GRENADE_COOLDOWN;
            game.addGrenade(new Grenade(robot.getX(), robot.getY(),
                directionDegrees, Color.RED,
                robot.getController().getName()));
            return true;
        }

        @Override
        public void incSpeed() { robot.incSpeed(); }

        @Override
        public void decSpeed() { robot.decSpeed(); }

        @Override
        public void turnClockwise() { robot.turnClockwise(); }

        @Override
        public void turnCounterClockwise() { robot.turnCounterClockwise(); }

        @Override
        public double getX() { return robot.getX(); }

        @Override
        public double getY() { return robot.getY(); }

        @Override
        public double getDirection() { return Math.toDegrees(robot.getDirection()); }

        @Override
        public double getSpeed() { return robot.getSpeed(); }

        @Override
        public double getHealth() { return robot.getHealth(); }

        @Override
        public int getArenaWidth() { return game.getArenaWidth(); }

        @Override
        public int getArenaHeight() { return game.getArenaHeight(); }

        @Override
        public Obstacle[] getObstacles() { return game.getObstacles(); }

        @Override
        public Grenade[] getGrenades() { return game.getGrenades(); }

        @Override
        public Heart[] getHearts() { return game.getHearts(); }

        @Override
        public String getLastScanHitRobotName() {
            return robot.getLastScanHitRobotName();
        }

        @Override
        public ScanHitType getLastScanHitType() {
            return robot.getLastScanHitType();
        }

        @Override
        public int getAliveRobotCount() { return game.getAliveRobotCount(); }

        @Override
        public void clearEstimatedTarget() { robot.clearEstimatedTarget(); }
    }
}
