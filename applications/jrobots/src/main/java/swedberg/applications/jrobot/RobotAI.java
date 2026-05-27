package swedberg.applications.jrobot;

import java.util.Random;
import swedberg.framework.utilities.PositionTracker;

public class RobotAI implements RobotController {
    private static final double OBSTACLE_MARGIN = 55;
    private static final double GRENADE_FLEE_MARGIN = 100;

    enum Strategy { AGGRESSIVE, SNIPER, EVASIVE }

    private enum State { EXPLORE, SEARCH, ATTACK, EVADE }

    private final Random rand = new Random();
    private final PositionTracker tracker = new PositionTracker();
    private final String name;
    private final Strategy strategy;
    private State state = State.EXPLORE;
    private int stateTimer;
    private double desiredDir;
    private double scanAngle;
    private double scanSweep = 1;
    private int shootBurst;
    private int dodgeDir = 1;
    private int desiredSpeed;
    private String targetName;
    private double heartTargetX;
    private double heartTargetY;
    private boolean hasHeartTarget;

    public RobotAI() { this("AI", Strategy.AGGRESSIVE); }

    public RobotAI(String name, Strategy strategy) {
        this.name = name;
        this.strategy = strategy;
    }

    @Override
    public String getName() { return name; }

    @Override
    public void tick(GameAPI api) {
        doScan(api);
        switch (state) {
            case EXPLORE -> tickExplore(api);
            case SEARCH -> tickSearch(api);
            case ATTACK -> tickAttack(api);
            case EVADE -> tickEvade(api);
        }
        if (hasHeartTarget) seekHeart(api);
        if (strategy == Strategy.AGGRESSIVE && state == State.ATTACK
            && api.getHealth() < 15 && api.getAliveRobotCount() > 2) {
            state = State.EVADE;
            stateTimer = 20;
        } else if (strategy != Strategy.AGGRESSIVE && api.getHealth() < 30
            && state != State.ATTACK && api.getAliveRobotCount() > 2) {
            state = State.EVADE;
            stateTimer = 40 + rand.nextInt(40);
        }
        avoidGrenades(api);
        avoidObstacles(api);
        turnToward(api, desiredDir);
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
        double result = api.scan((int) scanAngle % 360);
        if (result >= 0 && api.getLastScanHitType() == ScanHitType.HEART) {
            heartTargetX = api.getX() + Math.cos(Math.toRadians(scanAngle)) * result;
            heartTargetY = api.getY() + Math.sin(Math.toRadians(scanAngle)) * result;
            hasHeartTarget = true;
        } else if (result >= 0 && api.getLastScanHitType() == ScanHitType.ROBOT) {
            String hit = api.getLastScanHitRobotName();
            if (hit != null && !hit.equals(targetName)) {
                tracker.reset();
                targetName = hit;
            }
            tracker.recordHit(scanAngle, result, api.getX(), api.getY());
            if (state != State.ATTACK && state != State.EVADE) {
                state = State.ATTACK;
                stateTimer = strategy == Strategy.SNIPER ? 100 : 60;
            }
        } else if (result == -2) {
            return;
        } else if (result >= 0 || !tracker.hasEstimate()) {
            targetName = null;
        }
    }

    private void tickExplore(GameAPI api) {
        if (--stateTimer <= 0) {
            desiredDir = rand.nextDouble() * 360;
            desiredSpeed = switch (strategy) {
                case AGGRESSIVE -> 10 + rand.nextInt(6);
                case SNIPER -> 6 + rand.nextInt(4);
                case EVASIVE -> 12 + rand.nextInt(6);
            };
            stateTimer = 50 + rand.nextInt(50);
        }
        if (rand.nextInt(30) == 0) {
            state = State.SEARCH;
            stateTimer = strategy == Strategy.SNIPER ? 120 : 80;
        }
        scanAngle = (scanAngle + 15) % 360;
    }

    private void tickSearch(GameAPI api) {
        if (--stateTimer <= 0) {
            state = State.EXPLORE;
            return;
        }
        desiredSpeed = strategy == Strategy.AGGRESSIVE ? 7 : 4;
        scanAngle += scanSweep * (strategy == Strategy.SNIPER ? 4 : 8);
        if (scanAngle > 360 || scanAngle < 0) {
            scanSweep = -scanSweep;
            scanAngle = Math.max(0, Math.min(360, scanAngle));
            desiredDir = (desiredDir + 30 + rand.nextDouble() * 60) % 360;
        }
    }

    private void tickAttack(GameAPI api) {
        if (--stateTimer <= 0) {
            tracker.reset();
            state = State.SEARCH;
            stateTimer = strategy == Strategy.SNIPER ? 80 : 60;
            return;
        }
        aimAtTarget(api);
        desiredSpeed = switch (strategy) {
            case AGGRESSIVE -> 15;
            case SNIPER -> distToTarget(api) < 300 ? 14 : 8;
            case EVASIVE -> 14;
        };
        fireWeapon(api);
        tryThrowGrenade(api);
        scanAngle = scanAngle + 10;
        if (strategy == Strategy.EVASIVE && rand.nextInt(10) == 0)
            dodgeDir = -dodgeDir;
    }

    private void aimAtTarget(GameAPI api) {
        if (!tracker.hasEstimate()) return;
        int lead = switch (strategy) {
            case AGGRESSIVE -> 5; case SNIPER -> 10; case EVASIVE -> 3;
        };
        double aim = tracker.predictAngle(api.getX(), api.getY(), lead);
        double dist = tracker.distanceTo(api.getX(), api.getY());
        if (strategy == Strategy.SNIPER && dist < 200) {
            desiredDir = aim + 180;
        } else {
            desiredDir = strategy == Strategy.EVASIVE
                ? aim + dodgeDir * (45 + rand.nextInt(45)) : aim;
        }
    }

    private void fireWeapon(GameAPI api) {
        switch (strategy) {
            case AGGRESSIVE -> { if (++shootBurst % 2 == 0) api.shoot(); }
            case SNIPER -> { if (++shootBurst % 5 == 0) api.shoot(); }
            case EVASIVE -> { if (++shootBurst % 4 == 0) api.shoot(); }
        }
    }

    private double distToTarget(GameAPI api) {
        return tracker.hasEstimate()
            ? tracker.distanceTo(api.getX(), api.getY()) : Double.MAX_VALUE;
    }

    private void tickEvade(GameAPI api) {
        if (--stateTimer <= 0) {
            state = State.EXPLORE;
            return;
        }
        if (strategy == Strategy.EVASIVE) {
            Obstacle cover = findCover(api);
            if (cover != null) {
                double cx = cover.getX() + cover.getSize() / 2;
                double cy = cover.getY() + cover.getSize() / 2;
                desiredDir = Math.toDegrees(
                    Math.atan2(cy - api.getY(), cx - api.getX()));
                desiredSpeed = 16 + rand.nextInt(5);
                scanAngle = (scanAngle + 25) % 360;
                return;
            }
        }
        dodgeDir = -dodgeDir;
        int offset = strategy == Strategy.EVASIVE ? 90 : 60;
        desiredDir = api.getDirection() + dodgeDir * offset
            + rand.nextDouble() * 30;
        desiredSpeed = switch (strategy) {
            case AGGRESSIVE -> 16 + rand.nextInt(3);
            case SNIPER -> 14 + rand.nextInt(3);
            case EVASIVE -> 18 + rand.nextInt(5);
        };
        scanAngle = (scanAngle + 25) % 360;
    }

    private Obstacle findCover(GameAPI api) {
        if (!tracker.hasEstimate()) return null;
        double ax = api.getX(), ay = api.getY();
        double tx = tracker.getEstX(), ty = tracker.getEstY();
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

    private void avoidObstacles(GameAPI api) {
        double x = api.getX(), y = api.getY();
        double pushX = 0, pushY = 0;
        for (Obstacle o : api.getObstacles()) {
            double cx = o.getX() + o.getSize() / 2;
            double cy = o.getY() + o.getSize() / 2;
            double dx = x - cx;
            double dy = y - cy;
            double dist = Math.hypot(dx, dy);
            if (dist < OBSTACLE_MARGIN + o.getSize() / 2) {
                double strength = 1 - (dist - o.getSize() / 2) / OBSTACLE_MARGIN;
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

    private void tryThrowGrenade(GameAPI api) {
        if (!tracker.hasEstimate()) return;
        if (rand.nextInt(4) != 0) return;
        double dist = tracker.distanceTo(api.getX(), api.getY());
        if (dist < Grenade.MAX_THROW_DISTANCE) {
            double a = tracker.angleTo(api.getX(), api.getY());
            api.throwGrenade(a);
        }
    }
}
