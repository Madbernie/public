package swedberg.applications.jrobot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public enum RobotPattern {
    STRIPES {
        @Override public void draw(Graphics2D g2d, int cx, int cy, int r, Color base) {
            g2d.setClip(cx - r, cy - r, r * 2, r * 2);
            g2d.setColor(base);
            g2d.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2d.setColor(contrast(base));
            g2d.setStroke(new BasicStroke(r / 6f));
            for (int i = -r * 2; i < r * 2; i += r / 3) {
                g2d.drawLine(cx + i, cy - r * 2, cx + i + r, cy + r * 2);
            }
            g2d.setClip(null);
            drawBorder(g2d, cx, cy, r, base);
        }
    },

    POLKA {
        @Override public void draw(Graphics2D g2d, int cx, int cy, int r, Color base) {
            g2d.setColor(base);
            g2d.fillOval(cx - r, cy - r, r * 2, r * 2);
            int dotR = Math.max(2, r / 6);
            g2d.setColor(contrast(base));
            for (int y = -r + dotR * 2; y < r; y += dotR * 4) {
                for (int x = -r + dotR * 2; x < r; x += dotR * 4) {
                    if (x * x + y * y < (r - dotR) * (r - dotR)) {
                        g2d.fillOval(cx + x - dotR, cy + y - dotR, dotR * 2, dotR * 2);
                    }
                }
            }
            drawBorder(g2d, cx, cy, r, base);
        }
    },

    CHECKER {
        @Override public void draw(Graphics2D g2d, int cx, int cy, int r, Color base) {
            int step = Math.max(2, r / 5);
            Color c2 = contrast(base);
            for (int y = cy - r; y < cy + r; y += step) {
                for (int x = cx - r; x < cx + r; x += step) {
                    if ((x - cx + r) / step % 2 == (y - cy + r) / step % 2) continue;
                    double dx = x - cx + step / 2.0;
                    double dy = y - cy + step / 2.0;
                    if (dx * dx + dy * dy < r * r) {
                        g2d.setColor(c2);
                        g2d.fillRect(x, y, step, step);
                    }
                }
            }
            g2d.setColor(base);
            g2d.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2d.setClip(cx - r, cy - r, r * 2, r * 2);
            for (int y = cy - r; y < cy + r; y += step) {
                for (int x = cx - r; x < cx + r; x += step) {
                    if ((x - cx + r) / step % 2 == (y - cy + r) / step % 2) {
                        g2d.setColor(c2);
                        g2d.fillRect(x, y, step, step);
                    }
                }
            }
            g2d.setClip(null);
            drawBorder(g2d, cx, cy, r, base);
        }
    },

    TARGET {
        @Override public void draw(Graphics2D g2d, int cx, int cy, int r, Color base) {
            Color c2 = contrast(base);
            int rings = 5;
            for (int i = rings; i >= 0; i--) {
                int ir = r * (i + 1) / (rings + 1);
                g2d.setColor(i % 2 == 0 ? base : c2);
                g2d.setStroke(new BasicStroke(r / 4f));
                g2d.drawOval(cx - ir, cy - ir, ir * 2, ir * 2);
            }
            g2d.setColor(c2);
            int dot = Math.max(2, r / 8);
            g2d.fillOval(cx - dot, cy - dot, dot * 2, dot * 2);
            drawBorder(g2d, cx, cy, r, base);
        }
    },

    ZEBRA {
        @Override public void draw(Graphics2D g2d, int cx, int cy, int r, Color base) {
            g2d.setClip(cx - r, cy - r, r * 2, r * 2);
            g2d.setColor(base);
            g2d.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2d.setColor(contrast(base));
            g2d.setStroke(new BasicStroke(r / 4f));
            for (int i = -r; i < r; i += r / 4) {
                g2d.drawLine(cx - r, cy + i, cx + r, cy + i);
            }
            g2d.setClip(null);
            drawBorder(g2d, cx, cy, r, base);
        }
    },

    SPIRAL {
        @Override public void draw(Graphics2D g2d, int cx, int cy, int r, Color base) {
            g2d.setColor(base);
            g2d.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2d.setColor(contrast(base));
            g2d.setStroke(new BasicStroke(Math.max(1, r / 10f)));
            for (int i = 0; i < 360 * 3; i += 15) {
                double a = Math.toRadians(i);
                double nr = r * i / (360.0 * 3);
                int x1 = cx + (int)(Math.cos(a) * nr);
                int y1 = cy + (int)(Math.sin(a) * nr);
                int x2 = cx + (int)(Math.cos(a) * (nr + r / 20));
                int y2 = cy + (int)(Math.sin(a) * (nr + r / 20));
                g2d.drawLine(x1, y1, x2, y2);
            }
            drawBorder(g2d, cx, cy, r, base);
        }
    };

    public abstract void draw(Graphics2D g2d, int cx, int cy, int r, Color base);

    private static void drawBorder(Graphics2D g2d, int cx, int cy, int r, Color base) {
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.WHITE);
        g2d.drawOval(cx - r, cy - r, r * 2, r * 2);
    }

    private static Color contrast(Color c) {
        int r = c.getRed(), g = c.getGreen(), b = c.getBlue();
        int lum = (r * 299 + g * 587 + b * 114) / 1000;
        return lum > 128 ? Color.BLACK : Color.WHITE;
    }
}
