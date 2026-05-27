package swedberg.applications.jrobot;

public class SpeedContext {
    public double health;
    public double aggression;
    public double targetDistance;
    public double targetSpeed;
    public String modeName;
    public double currentSpeed;

    public void update(double h, double a, double dist, double tSpeed,
        String mode, double curSpeed) {
        health = h;
        aggression = a;
        targetDistance = dist;
        targetSpeed = tSpeed;
        modeName = mode;
        currentSpeed = curSpeed;
    }
}
