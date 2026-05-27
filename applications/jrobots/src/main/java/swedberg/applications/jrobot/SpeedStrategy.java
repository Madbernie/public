package swedberg.applications.jrobot;

public interface SpeedStrategy {
    int desiredSpeed(SpeedContext ctx);
    String name();
}
