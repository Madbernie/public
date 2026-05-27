package swedberg.applications.jrobot;

public interface RobotController {
    String getName();
    void tick(GameAPI api);
    default void onGameOver(boolean won, double healthRemaining) {}
}
