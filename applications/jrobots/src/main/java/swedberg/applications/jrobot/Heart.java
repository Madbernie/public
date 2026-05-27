package swedberg.applications.jrobot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class Heart {
    static final double RADIUS = 15;
    static final int SPAWN_INTERVAL = 1000;
    static final double HEAL_AMOUNT = 5;

    private final double x;
    private final double y;
    private boolean alive = true;

    public Heart(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return RADIUS; }
    public boolean isAlive() { return alive; }
    public void collect() { alive = false; }

    public void draw(Graphics2D g2d) {
        if (!alive) return;
        int cx = (int) x;
        int cy = (int) y;
        int r = (int) RADIUS;
        int lobeR = r;

        g2d.setStroke(new BasicStroke(1));

        g2d.setColor(new Color(220, 40, 40));
        g2d.fillOval(cx - lobeR, cy - r / 2, lobeR, lobeR);
        g2d.fillOval(cx, cy - r / 2, lobeR, lobeR);
        int[] xs = { cx - lobeR, cx + lobeR, cx };
        int[] ys = { cy - r / 4, cy - r / 4, cy + r };
        g2d.fillPolygon(xs, ys, 3);

        g2d.setColor(new Color(255, 100, 100));
        g2d.drawOval(cx - lobeR, cy - r / 2, lobeR, lobeR);
        g2d.drawOval(cx, cy - r / 2, lobeR, lobeR);
        g2d.drawPolygon(xs, ys, 3);
    }
}
