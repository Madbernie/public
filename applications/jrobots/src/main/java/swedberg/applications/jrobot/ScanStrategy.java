package swedberg.applications.jrobot;

public interface ScanStrategy {
    double nextAngle(ScanContext ctx);
    void reset();
    boolean isDone();
    String name();
}
