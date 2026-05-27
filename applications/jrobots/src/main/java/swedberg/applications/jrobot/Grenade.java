package swedberg.applications.jrobot;

import java.awt.Color;

public class Grenade {
    static final double RADIUS = 6;
    static final double SPEED = 6.0;
    static final double MAX_DISTANCE = 200.0;
    static final double MAX_THROW_DISTANCE = 150.0;
    static final double BLAST_RADIUS = 40.0;
    static final double DAMAGE = 2;
    static final int EXPLOSION_DURATION = 30;
    static final int COOLDOWN = 50;

    private double x;
    private double y;
    private final double startX;
    private final double startY;
    private final double directionRad;
    private final Color color;
    private final String throwerName;
    private boolean alive = true;
    private boolean exploded;
    private int explosionTimer;
    private double traveled;

    public Grenade(double x, double y, double directionDeg, Color color, String throwerName) {
        this.x = x;
        this.y = y;
        this.startX = x;
        this.startY = y;
        this.directionRad = Math.toRadians(directionDeg);
        this.color = color;
        this.throwerName = throwerName;
    }

    public void move() {
        if (exploded) {
            explosionTimer--;
            return;
        }
        double step = SPEED;
        x += Math.cos(directionRad) * step;
        y += Math.sin(directionRad) * step;
        traveled += step;
        if (traveled >= MAX_DISTANCE) explode();
    }

    public void explode() {
        if (exploded) return;
        exploded = true;
        explosionTimer = EXPLOSION_DURATION;
    }

    public boolean isAlive() {
        return alive && (exploded ? explosionTimer > 0 : true);
    }

    public boolean isExploded() { return exploded; }

    public boolean isInFlight() { return !exploded && alive; }

    public double getBlastCenterX() { return x; }

    public double getBlastCenterY() { return y; }

    public double getBlastRadius() { return BLAST_RADIUS; }

    public int getExplosionTimer() { return explosionTimer; }

    public double getX() { return x; }

    public double getY() { return y; }

    public double getDirectionDeg() { return Math.toDegrees(directionRad); }

    public Color getColor() { return color; }

    public String getThrowerName() { return throwerName; }
}
