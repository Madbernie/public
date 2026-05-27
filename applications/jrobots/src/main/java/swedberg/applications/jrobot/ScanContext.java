package swedberg.applications.jrobot;

import java.util.List;

public class ScanContext {
    public double robotX;
    public double robotY;
    public double arenaWidth;
    public double arenaHeight;
    public double targetEstX;
    public double targetEstY;
    public boolean hasEstimate;
    public double targetVelX;
    public double targetVelY;
    public boolean hasVelocity;
    public int missesSinceHit;
    public int ticksSinceHit;
    public double lastAngle;
    public int tickCount;
    public double lastScanResult;
    public List<Grenade> grenades;
    public List<Obstacle> obstacles;

    public void update(GameAPI api, double targetX, double targetY, boolean hasTarget,
        double velX, double velY, boolean hasVel, int misses, int ticks, int tick,
        double previousAngle, double scanResult) {
        robotX = api.getX();
        robotY = api.getY();
        arenaWidth = api.getArenaWidth();
        arenaHeight = api.getArenaHeight();
        targetEstX = targetX;
        targetEstY = targetY;
        hasEstimate = hasTarget;
        targetVelX = velX;
        targetVelY = velY;
        hasVelocity = hasVel;
        missesSinceHit = misses;
        ticksSinceHit = ticks;
        lastAngle = previousAngle;
        tickCount = tick;
        lastScanResult = scanResult;
        grenades = List.of(api.getGrenades());
        obstacles = List.of(api.getObstacles());
    }
}
