package swedberg.applications.jrobot;

import java.awt.Color;

public class Projectile {
    static final double RADIUS = 5;
    static final double SPEED = 20.0;

    private double x;
    private double y;
    private final double direction;
    private final Color color;
    private boolean alive = true;

    public Projectile(double x, double y, double direction, Color color) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.color = color;
    }

    public void move(int arenaWidth, int arenaHeight) {
        x += Math.cos(direction) * SPEED;
        y += Math.sin(direction) * SPEED;
        if (x < 0 || x > arenaWidth || y < 0 || y > arenaHeight) {
            alive = false;
        }
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getDirection() { return direction; }
    public Color getColor() { return color; }
    public boolean isAlive() { return alive; }
    public void destroy() { alive = false; }
}
