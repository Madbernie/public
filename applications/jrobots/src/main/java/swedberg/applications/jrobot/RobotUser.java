package swedberg.applications.jrobot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import swedberg.framework.utilities.PositionTracker;

public class RobotUser implements RobotController {
    private static final double FOLLOW_DIST = 300;
    private static final double GRENADE_FLEE_MARGIN = 100;

    private final Random rand = new Random();
    private final Map<String, PositionTracker> trackers = new HashMap<>();
    private final String name;
    private final StrategyProfile strategy;

    private final ScanContext scanContext = new ScanContext();
    private final SpeedContext speedContext = new SpeedContext();
    private final StrategySequence sequence = new StrategySequence();
    private int lastPhase = -1;

    private double scanAngle;
    private int ticksSinceHit = 999;
    private int missesSinceHit = 999;
    private double desiredDir;
    private int desiredSpeed = 8;
    private int moveTimer;

    private enum Mode { CRUISE, FOLLOW, FLEE, SHOOT }
    private Mode mode = Mode.CRUISE;

    private boolean wantToShoot;
    private final TargetMemory targetMemory = new TargetMemory();
    private int dodgeTimer;
    private int dodgeSign = 1;
    private int shootTimer;
    private String targetName;
    private boolean searchStateReset;
    private double heartTargetX;
    private double heartTargetY;
    private boolean hasHeartTarget;
    private int scanTargetIndex;

    private double wallMinX = 18;
    private double wallMaxX = 1182;
    private double wallMinY = 18;
    private double wallMaxY = 882;

    public RobotUser() { this("Madbernie"); }

    public RobotUser(String name) {
        this.name = name;
        this.strategy = StrategyDatabase.selectProfile();
        sequence.add(BehaviourStrategy.BALANSERAD);
    }

    public RobotUser(String name, BehaviourStrategy behaviour) {
        this.name = name;
        this.strategy = StrategyDatabase.selectProfile();
        sequence.add(behaviour);
    }

    public String getBehaviourName() { return sequence.current().name(); }
    public String getStrategyInfo() {
        BehaviourStrategy b = sequence.current();
        return b.name() + " | " + b.scan().name() + " | " + b.speed().name()
            + " | " + b.targeting().name();
    }

    public void setSequence(BehaviourStrategy... strategies) {
        sequence.reset();
        for (BehaviourStrategy s : strategies)
            sequence.add(s);
    }

    @Override
    public String getName() { return name; }

    private int survivalTicks;

    @Override
    public void onGameOver(boolean won, double healthRemaining) {
        StrategyDatabase.recordResult(strategy, won, healthRemaining, survivalTicks);
    }

    private PositionTracker getPrimaryTracker() {
        return trackers.get(targetName);
    }

    private PositionTracker getOrCreateTracker(String name) {
        return trackers.computeIfAbsent(name, k -> new PositionTracker());
    }

    private void resetAllTrackers() {
        for (PositionTracker t : trackers.values()) t.reset();
    }

    private void resetTracker(String name) {
        PositionTracker t = trackers.get(name);
        if (t != null) t.reset();
    }

    private List<String> getActiveTargets(GameAPI api) {
        List<String> targets = new ArrayList<>();
        for (TargetMemory.Record r : targetMemory.getActiveRecords(survivalTicks, 300)) {
            if (!targets.contains(r.name)) targets.add(r.name);
        }
        return targets;
    }

    @Override
    public void tick(GameAPI api) {
        survivalTicks++;
        sequence.advance();
        checkPhaseTransition();
        learnWalls(api);
        doScan(api);
        ticksSinceHit++;
        chooseMode(api);
        executeMode(api);
        if (hasHeartTarget) seekHeart(api);
        updateSpeedContext(api);
        desiredSpeed = sequence.current().speed().desiredSpeed(speedContext);
        turnToward(api, desiredDir);
        fireIfWanted(api);
        tickDodge(api);
        avoidGrenades(api);
        avoidWalls(api);
        avoidObstacles(api);
        adjustSpeed(api);
    }

    private void seekHeart(GameAPI api) {
        boolean heartAlive = false;
        for (Heart h : api.getHearts()) {
            if (h.isAlive()) { heartAlive = true; break; }
        }
        if (!heartAlive) { hasHeartTarget = false; return; }
        double dx = heartTargetX - api.getX();
        double dy = heartTargetY - api.getY();
        desiredDir = Math.toDegrees(Math.atan2(dy, dx));
        desiredSpeed = 20;
        wantToShoot = false;
    }

    private void checkPhaseTransition() {
        if (lastPhase != sequence.phaseIndex()) {
            lastPhase = sequence.phaseIndex();
            sequence.current().scan().reset();
        }
    }

    private void executeMode(GameAPI api) {
        switch (mode) {
            case CRUISE -> tickCruise(api);
            case FOLLOW -> tickFollow(api);
            case FLEE -> tickFlee(api);
            case SHOOT -> tickShoot(api);
        }
    }

    private void updateSpeedContext(GameAPI api) {
        PositionTracker pt = getPrimaryTracker();
        speedContext.update(api.getHealth(), aggression(api),
            pt != null && pt.hasEstimate() ? pt.distanceTo(api.getX(), api.getY()) : 0,
            pt != null && pt.hasVelocity() ? Math.min(20, 2 + (int) pt.estimateSpeed()) : 0,
            mode.name(), api.getSpeed());
    }

    private void fireIfWanted(GameAPI api) {
        if (wantToShoot) {
            api.shoot();
            wantToShoot = false;
        }
    }

    private void learnWalls(GameAPI api) {
        wallMaxX = api.getArenaWidth() - 18;
        wallMaxY = api.getArenaHeight() - 18;
    }

    private void turnToward(GameAPI api, double target) {
        double diff = target - api.getDirection();
        while (diff > 180) diff -= 360;
        while (diff < -180) diff += 360;
        if (diff > 3) api.turnClockwise();
        else if (diff < -3) api.turnCounterClockwise();
    }

    private void adjustSpeed(GameAPI api) {
        double current = api.getSpeed();
        if (current < desiredSpeed) api.incSpeed();
        if (current > desiredSpeed) api.decSpeed();
    }

    private void doScan(GameAPI api) {
        pickScanAngle(api);
        double result = api.scan(scanAngle);
        if (result == -2) return;
        if (result >= 0 && api.getLastScanHitType() == ScanHitType.HEART) {
            handleScanHeart(api, result);
        } else if (result >= 0 && api.getLastScanHitType() == ScanHitType.ROBOT) {
            handleScanHit(api, result);
        } else {
            handleScanMiss(api, result);
        }
    }

    private void pickScanAngle(GameAPI api) {
        if (missesSinceHit == 999) return;
        if (missesSinceHit < 3) return;
        if (targetName != null && missesSinceHit < 6) {
            PositionTracker pt = getPrimaryTracker();
            if (pt != null && pt.hasEstimate()) {
                scanAngle = pt.angleTo(api.getX(), api.getY());
                return;
            }
        }
        List<String> active = getActiveTargets(api);
        if (active.isEmpty()) {
            scanAngle = scanAngleNotAtWall(
                sequence.current().scan().nextAngle(scanContext), api);
            return;
        }
        scanTargetIndex = (scanTargetIndex + 1) % active.size();
        String st = active.get(scanTargetIndex);
        PositionTracker t = trackers.get(st);
        if (t != null && t.hasEstimate()) {
            scanAngle = t.angleTo(api.getX(), api.getY());
        } else {
            scanAngle = scanAngleNotAtWall(
                sequence.current().scan().nextAngle(scanContext), api);
        }
    }

    private void handleScanHeart(GameAPI api, double result) {
        heartTargetX = api.getX() + Math.cos(Math.toRadians(scanAngle)) * result;
        heartTargetY = api.getY() + Math.sin(Math.toRadians(scanAngle)) * result;
        hasHeartTarget = true;
    }

    private void handleScanHit(GameAPI api, double result) {
        String hit = api.getLastScanHitRobotName();
        if (hit != null) {
            double hx = api.getX() + Math.cos(Math.toRadians(scanAngle)) * result;
            double hy = api.getY() + Math.sin(Math.toRadians(scanAngle)) * result;
            targetMemory.recordHit(hit, hx, hy, survivalTicks);
            String chosen = sequence.current().targeting()
                .selectTarget(targetName, targetMemory.getActiveRecords(survivalTicks, 300));
            if (chosen != null && !chosen.equals(targetName)) {
                targetName = chosen;
                api.clearEstimatedTarget();
            }
            getOrCreateTracker(hit).recordHit(scanAngle, result, api.getX(), api.getY());
        }
        ticksSinceHit = 0;
        missesSinceHit = 0;
        searchStateReset = false;
        sequence.current().scan().reset();
        updateScanContext(api, 0, 0, result);
    }

    private void handleScanMiss(GameAPI api, double result) {
        missesSinceHit++;
        if (missesSinceHit < 3) return;
        updateScanContext(api, missesSinceHit, ticksSinceHit, result);
        if (!searchStateReset && (sequence.current().scan().isDone() || missesSinceHit >= 30)) {
            if (getPrimaryTracker() != null) resetTracker(targetName);
            targetName = null;
            api.clearEstimatedTarget();
            sequence.current().scan().reset();
            searchStateReset = true;
        }
        scanAngle = scanAngleNotAtWall(sequence.current().scan().nextAngle(scanContext), api);
    }

    private void updateScanContext(GameAPI api, int misses, int ticks, double result) {
        PositionTracker pt = getPrimaryTracker();
        double tx = pt != null && pt.hasEstimate() ? pt.getEstX() : 0;
        double ty = pt != null && pt.hasEstimate() ? pt.getEstY() : 0;
        double vx = pt != null && pt.hasVelocity() ? pt.getEstX() - pt.getPrevEstX() : 0;
        double vy = pt != null && pt.hasVelocity() ? pt.getEstY() - pt.getPrevEstY() : 0;
        scanContext.update(api, tx, ty, pt != null && pt.hasEstimate(),
            vx, vy, pt != null && pt.hasVelocity(), misses, ticks, survivalTicks, scanAngle, result);
    }

    private double scanAngleNotAtWall(double angle, GameAPI api) {
        for (int i = 0; i < 36; i++) {
            double rad = Math.toRadians(angle);
            double nx = api.getX() + Math.cos(rad) * 80;
            double ny = api.getY() + Math.sin(rad) * 80;
            boolean blocked = nx < wallMinX || nx > wallMaxX
                || ny < wallMinY || ny > wallMaxY;
            if (!blocked) {
                for (Obstacle o : api.getObstacles()) {
                    if (nx >= o.getX() && nx <= o.getX() + o.getSize()
                        && ny >= o.getY() && ny <= o.getY() + o.getSize()) {
                        blocked = true;
                        break;
                    }
                }
            }
            if (!blocked) return angle;
            angle = (angle + 10) % 360;
        }
        return angle;
    }

    private double aggression(GameAPI api) {
        double h = api.getHealth();
        double base;
        if (h >= 60) base = 1.0;
        else if (h <= 30) base = 0.0;
        else base = (h - 30) / 30;
        return Math.min(base, sequence.current().maxAggression());
    }

    private int shakeDist(GameAPI api) {
        return (int)(strategy.fleeDist + 50 * (1 - aggression(api)));
    }

    private void chooseMode(GameAPI api) {
        if (missesSinceHit < 3) {
            mode = Mode.SHOOT;
            return;
        }
        PositionTracker pt = getPrimaryTracker();
        if (pt == null || !pt.hasEstimate() || ticksSinceHit >= 120) {
            mode = Mode.CRUISE;
            return;
        }
        double dist = pt.distanceTo(api.getX(), api.getY());
        if (dist < shakeDist(api)) {
            mode = Mode.FLEE;
        } else {
            mode = Mode.FOLLOW;
        }
    }

    private void tickCruise(GameAPI api) {
        if (--moveTimer <= 0) {
            desiredDir = rand.nextDouble() * 360;
            moveTimer = 40 + rand.nextInt(40);
        }
        PositionTracker pt = getPrimaryTracker();
        if (pt != null && pt.hasEstimate()) {
            double dist = pt.distanceTo(api.getX(), api.getY());
            if (dist < 600)
                desiredDir = pt.angleTo(api.getX(), api.getY());
        }
    }

    private void tickFollow(GameAPI api) {
        PositionTracker pt = getPrimaryTracker();
        if (pt == null) return;
        double dist = pt.distanceTo(api.getX(), api.getY());
        double a = pt.angleTo(api.getX(), api.getY());

        if (dist > FOLLOW_DIST + 150) {
            desiredDir = a;
            wantToShoot = true;
        } else if (dist < FOLLOW_DIST - 100) {
            desiredDir = a + 180;
        } else {
            desiredDir = a + 60 * dodgeSign;
            wantToShoot = true;
        }
    }

    private void tickFlee(GameAPI api) {
        PositionTracker pt = getPrimaryTracker();
        if (pt == null) return;
        double a = pt.angleTo(api.getX(), api.getY());

        Obstacle cover = findCover(api);
        if (cover != null) {
            double cx = cover.getX() + cover.getSize() / 2;
            double cy = cover.getY() + cover.getSize() / 2;
            double behindX = cx + (cx - api.getX()) * 1.5;
            double behindY = cy + (cy - api.getY()) * 1.5;
            desiredDir = Math.toDegrees(Math.atan2(behindY - api.getY(), behindX - api.getX()));
        } else {
            desiredDir = a + 135;
        }
    }

    private Obstacle findCover(GameAPI api) {
        PositionTracker pt = getPrimaryTracker();
        if (pt == null || !pt.hasEstimate()) return null;
        double ax = api.getX(), ay = api.getY();
        double tx = pt.getEstX(), ty = pt.getEstY();
        Obstacle best = null;
        double bestDist = Double.MAX_VALUE;
        for (Obstacle o : api.getObstacles()) {
            double cx = o.getX() + o.getSize() / 2;
            double cy = o.getY() + o.getSize() / 2;
            double d = Math.hypot(cx - ax, cy - ay);
            double toTarget = Math.hypot(tx - ax, ty - ay);
            if (d > toTarget - 80 || d > 300) continue;
            if (d < bestDist) { bestDist = d; best = o; }
        }
        return best;
    }

    private void tickShoot(GameAPI api) {
        PositionTracker pt = getPrimaryTracker();
        if (pt == null) return;
        double dist = pt.distanceTo(api.getX(), api.getY());
        double aaa = aggression(api);
        int lead = Math.min(10, Math.max(1, (int)(dist / (30 + 40 * aaa))));
        double a = pt.hasVelocity()
            ? pt.predictAngle(api.getX(), api.getY(), lead) : pt.angleTo(api.getX(), api.getY());

        if (++shootTimer > 15) {
            desiredDir = a;
            maybeThrowGrenade(api);
            wantToShoot = true;
            if (shootTimer > 20) shootTimer = 0;
        } else {
            desiredDir = a + 60 * dodgeSign;
        }
    }

    private void tickDodge(GameAPI api) {
        if (mode == Mode.SHOOT && shootTimer > 15) return;
        if (--dodgeTimer > 0) {
            double aaa = aggression(api);
            int juke = (int)(60 + 30 * (1 - aaa));
            desiredDir += dodgeSign * juke;
            return;
        }
        if (mode == Mode.FOLLOW || mode == Mode.SHOOT) {
            dodgeSign = -dodgeSign;
            dodgeTimer = strategy.dodgeInterval + rand.nextInt(8);
        }
    }

    private void avoidWalls(GameAPI api) {
        double x = api.getX();
        double y = api.getY();

        double pushX = 0;
        double pushY = 0;
        if (x < wallMinX + strategy.wallMargin) pushX += 1 - (x - wallMinX) / strategy.wallMargin;
        if (x > wallMaxX - strategy.wallMargin) pushX -= 1 - (wallMaxX - x) / strategy.wallMargin;
        if (y < wallMinY + strategy.wallMargin) pushY += 1 - (y - wallMinY) / strategy.wallMargin;
        if (y > wallMaxY - strategy.wallMargin) pushY -= 1 - (wallMaxY - y) / strategy.wallMargin;

        if (pushX == 0 && pushY == 0) return;

        double pushAngle = Math.toDegrees(Math.atan2(pushY, pushX));
        double strength = Math.min(1, Math.hypot(pushX, pushY));
        if (strength > 0.8) {
            desiredDir = pushAngle;
            desiredSpeed = Math.min(desiredSpeed, 4);
        } else if (strength > 0.4) {
            double diff = pushAngle - desiredDir;
            while (diff > 180) diff -= 360;
            while (diff < -180) diff += 360;
            desiredDir += diff * strength * 0.5;
            desiredSpeed = Math.min(desiredSpeed, 4);
        }
    }

    private void avoidObstacles(GameAPI api) {
        double x = api.getX(), y = api.getY();
        double pushX = 0, pushY = 0;
        for (Obstacle o : api.getObstacles()) {
            double cx = o.getX() + o.getSize() / 2;
            double cy = o.getY() + o.getSize() / 2;
            double dx = x - cx;
            double dy = y - cy;
            double dist = Math.hypot(dx, dy);
            if (dist < strategy.obstacleMargin + o.getSize() / 2) {
                double strength = 1 - (dist - o.getSize() / 2) / strategy.obstacleMargin;
                pushX += (dx / dist) * strength;
                pushY += (dy / dist) * strength;
            }
        }
        if (pushX == 0 && pushY == 0) return;
        double pushAngle = Math.toDegrees(Math.atan2(pushY, pushX));
        double strength = Math.min(1, Math.hypot(pushX, pushY));
        if (strength > 0.5) {
            desiredDir = pushAngle;
            desiredSpeed = Math.min(desiredSpeed, 3);
        } else {
            double diff = pushAngle - desiredDir;
            while (diff > 180) diff -= 360;
            while (diff < -180) diff += 360;
            desiredDir += diff * strength;
            desiredSpeed = Math.min(desiredSpeed, 4);
        }
    }

    private void avoidGrenades(GameAPI api) {
        double x = api.getX(), y = api.getY();
        double nearestDx = 0, nearestDy = 0;
        double nearestDist = Double.MAX_VALUE;
        for (Grenade g : api.getGrenades()) {
            double dx = x - g.getBlastCenterX();
            double dy = y - g.getBlastCenterY();
            double dist = Math.hypot(dx, dy) - g.getBlastRadius();
            if (dist < GRENADE_FLEE_MARGIN) {
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearestDx = dx;
                    nearestDy = dy;
                }
            }
        }
        if (nearestDist >= Double.MAX_VALUE) return;
        desiredDir = Math.toDegrees(Math.atan2(nearestDy, nearestDx));
        desiredSpeed = Math.max(desiredSpeed, 14);
    }

    private void maybeThrowGrenade(GameAPI api) {
        PositionTracker pt = getPrimaryTracker();
        if (pt == null || !pt.hasEstimate()) return;
        if (rand.nextInt(5) != 0) return;
        double dist = pt.distanceTo(api.getX(), api.getY());
        if (dist < Grenade.MAX_THROW_DISTANCE) {
            double a = pt.angleTo(api.getX(), api.getY());
            api.throwGrenade(a);
        }
    }
}
