package swedberg.framework.utilities;

public class PositionTracker {
    private static final int MAX_HISTORY = 5;
    private final double[] histX = new double[MAX_HISTORY];
    private final double[] histY = new double[MAX_HISTORY];
    private int histCount;
    private double estX;
    private double estY;
    private double prevEstX;
    private double prevEstY;
    private boolean hasEstimate;

    public void recordHit(double scanAngleDeg, double distance, double myX, double myY) {
        prevEstX = estX;
        prevEstY = estY;
        double rad = Math.toRadians(scanAngleDeg);
        double gx = myX + Math.cos(rad) * distance;
        double gy = myY + Math.sin(rad) * distance;
        histX[histCount % MAX_HISTORY] = gx;
        histY[histCount % MAX_HISTORY] = gy;
        histCount++;
        int n = Math.min(histCount, MAX_HISTORY);
        double sx = 0, sy = 0;
        for (int i = 0; i < n; i++) {
            sx += histX[i];
            sy += histY[i];
        }
        double avgX = sx / n;
        double avgY = sy / n;
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double t = cos * (avgX - myX) + sin * (avgY - myY);
        if (t < 0) t = 0;
        estX = myX + cos * t;
        estY = myY + sin * t;
        hasEstimate = true;
    }

    public double getEstX() { return estX; }
    public double getEstY() { return estY; }
    public double getPrevEstX() { return prevEstX; }
    public double getPrevEstY() { return prevEstY; }
    public boolean hasEstimate() { return hasEstimate; }

    public double distanceTo(double myX, double myY) {
        return Math.hypot(estX - myX, estY - myY);
    }

    public double angleTo(double myX, double myY) {
        return Math.toDegrees(Math.atan2(estY - myY, estX - myX));
    }

    public double predictAngle(double myX, double myY, double lead) {
        double velX = estX - prevEstX;
        double velY = estY - prevEstY;
        double predX = estX + velX * lead;
        double predY = estY + velY * lead;
        return Math.toDegrees(Math.atan2(predY - myY, predX - myX));
    }

    public double estimateSpeed() {
        return Math.hypot(estX - prevEstX, estY - prevEstY);
    }

    public boolean hasVelocity() {
        return histCount >= 2;
    }

    public void reset() {
        histCount = 0;
        hasEstimate = false;
    }
}
