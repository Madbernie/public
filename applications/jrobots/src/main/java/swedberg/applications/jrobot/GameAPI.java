package swedberg.applications.jrobot;

public interface GameAPI {
    double scan(double directionDegrees);
    boolean shoot();
    boolean throwGrenade(double directionDegrees);
    void incSpeed();
    void decSpeed();
    void turnClockwise();
    void turnCounterClockwise();
    double getX();
    double getY();
    double getDirection();
    double getSpeed();
    double getHealth();
    int getArenaWidth();
    int getArenaHeight();
    Obstacle[] getObstacles();
    Grenade[] getGrenades();
    Heart[] getHearts();
    String getLastScanHitRobotName();
    ScanHitType getLastScanHitType();
    int getAliveRobotCount();
    void clearEstimatedTarget();
}
