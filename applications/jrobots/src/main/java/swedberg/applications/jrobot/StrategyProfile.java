package swedberg.applications.jrobot;

public class StrategyProfile {
    public final int cruiseSpeed;
    public final double wallMargin;
    public final double obstacleMargin;
    public final int fleeDist;
    public final int dodgeInterval;
    public final int scanStep;
    public int gamesPlayed;
    public double totalScore;

    public StrategyProfile(int cruiseSpeed, double wallMargin,
        double obstacleMargin, int fleeDist, int dodgeInterval, int scanStep) {
        this.cruiseSpeed = cruiseSpeed;
        this.wallMargin = wallMargin;
        this.obstacleMargin = obstacleMargin;
        this.fleeDist = fleeDist;
        this.dodgeInterval = dodgeInterval;
        this.scanStep = scanStep;
    }

    public void recordResult(boolean won, double healthRemaining, long survivalTicks) {
        gamesPlayed++;
        if (won) {
            totalScore += 1000.0 + healthRemaining * 10.0 + survivalTicks * 0.5;
        } else {
            totalScore -= 500.0 + (100.0 - healthRemaining) * 5.0 + survivalTicks * 0.2;
        }
    }

    public double score() {
        if (gamesPlayed == 0) return Double.MIN_VALUE;
        return totalScore / gamesPlayed;
    }
}
