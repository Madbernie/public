package swedberg.applications.jrobot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.Timer;

public class JRobotPanel extends JPanel implements ActionListener {
    private final JRobotGame game;
    private final Timer timer;
    private static final int DELAY = 20;
    private static final int AUTO_RESTART_TICKS = 1000;
    private static final Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_BEVEL, 0, new float[]{4}, 0);
    private int restartTick;

    public JRobotPanel(JRobotGame game) {
        this.game = game;
        setBackground(new Color(20, 20, 30));
        setFocusable(true);
        timer = new Timer(DELAY, this);
        timer.start();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_R && game.isGameOver()) {
                    restartGame();
                }
            }
        });
    }

    private void restartGame() {
        JRobotGUI.startNewGame(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        game.update();
        repaint();
        if (game.isGameOver()) {
            if (++restartTick >= AUTO_RESTART_TICKS) restartGame();
        } else {
            restartTick = 0;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        game.setArenaSize(getWidth(), getHeight());
        game.ensureObstaclesGenerated();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        drawGrid(g2d);
        drawObstacles(g2d);
        drawEstimatedTargets(g2d);
        drawScanLines(g2d);
        drawHearts(g2d);
        drawGrenades(g2d);
        drawProjectiles(g2d);
        drawRobots(g2d);
        drawHUD(g2d);
        if (game.isGameOver()) drawGameOver(g2d);
    }

    private void drawGrid(Graphics2D g2d) {
        int w = getWidth();
        int h = getHeight();
        g2d.setColor(new Color(30, 30, 45));
        g2d.setStroke(dashed);
        for (int x = 0; x < w; x += 80) {
            g2d.drawLine(x, 0, x, h);
        }
        for (int y = 0; y < h; y += 80) {
            g2d.drawLine(0, y, w, y);
        }
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(new Color(60, 60, 80));
        g2d.drawRect(0, 0, w - 1, h - 1);
    }

    private void drawObstacles(Graphics2D g2d) {
        for (Obstacle o : game.getObstacles()) {
            int x = (int) o.getX();
            int y = (int) o.getY();
            int s = (int) o.getSize();
            g2d.setColor(new Color(70, 50, 30));
            g2d.fillRect(x, y, s, s);
            g2d.setColor(new Color(140, 90, 40));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(x, y, s, s);
        }
    }

    private void drawEstimatedTargets(Graphics2D g2d) {
        for (JRobot robot : game.getRobots()) {
            if (!robot.hasEstimatedTarget()) continue;
            int ax = (int) robot.getEstimatedTargetX();
            int ay = (int) robot.getEstimatedTargetY();
            double dx = ax - robot.getX();
            double dy = ay - robot.getY();
            double dist = Math.hypot(dx, dy);
            double angle = Math.toDegrees(Math.atan2(-dy, dx));
            double spread = 20 + robot.getEstimatedTargetAge() * 0.4;
            int alpha = Math.max(20, 60 - robot.getEstimatedTargetAge());
            Color c = robot.getColor();
            g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
            int d = (int) dist * 2;
            g2d.fillArc((int) robot.getX() - (int) dist,
                (int) robot.getY() - (int) dist,
                d, d, (int) (angle - spread / 2), (int) spread);
            g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(),
                Math.max(80, 200 - robot.getEstimatedTargetAge() * 2)));
            g2d.drawOval(ax - 6, ay - 6, 12, 12);
            g2d.drawLine(ax - 10, ay, ax + 10, ay);
            g2d.drawLine(ax, ay - 10, ax, ay + 10);
        }
    }

    private void drawScanLines(Graphics2D g2d) {
        for (JRobot robot : game.getRobots()) {
            if (robot.getScanVisualTimer() <= 0) continue;
            drawOneScanLine(g2d, robot);
        }
    }

    private void drawOneScanLine(Graphics2D g2d, JRobot robot) {
        double dir = robot.getLastScanDirection();
        ScanHitType hitType = robot.getLastScanHitType();
        double len = scanLineLength(robot);
        int x1 = (int) robot.getX();
        int y1 = (int) robot.getY();
        int x2 = (int) (robot.getX() + Math.cos(dir) * len);
        int y2 = (int) (robot.getY() + Math.sin(dir) * len);
        float alpha = Math.min(1, robot.getScanVisualTimer() / 12.0f);
        g2d.setColor(scanLineColor(hitType, alpha));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(x1, y1, x2, y2);
        drawScanHitPoint(g2d, robot, dir, len, hitType, alpha);
    }

    private double scanLineLength(JRobot robot) {
        ScanHitType hitType = robot.getLastScanHitType();
        if (hitType == ScanHitType.ROBOT || hitType == ScanHitType.HEART
            || hitType == ScanHitType.OBSTACLE || hitType == ScanHitType.WALL)
            return robot.getLastScanHitDistance();
        return Math.sqrt(getWidth() * getWidth() + getHeight() * getHeight());
    }

    private Color scanLineColor(ScanHitType hitType, float alpha) {
        if (hitType == ScanHitType.ROBOT) return new Color(100, 255, 100, (int)(alpha * 180));
        if (hitType == ScanHitType.HEART) return new Color(255, 80, 80, (int)(alpha * 200));
        if (hitType == ScanHitType.OBSTACLE) return new Color(200, 140, 60, (int)(alpha * 180));
        if (hitType == ScanHitType.WALL) return new Color(140, 140, 200, (int)(alpha * 180));
        return new Color(100, 255, 100, (int)(alpha * 80));
    }

    private void drawScanHitPoint(Graphics2D g2d, JRobot robot,
        double dir, double len, ScanHitType hitType, float alpha) {
        int x1 = (int) robot.getX();
        int y1 = (int) robot.getY();
        if (hitType == ScanHitType.ROBOT) {
            g2d.setColor(new Color(255, 255, 100, (int)(alpha * 200)));
            int dx = (int)(Math.cos(dir) * robot.getLastScanResult());
            int dy = (int)(Math.sin(dir) * robot.getLastScanResult());
            g2d.fillOval(x1 + dx - 4, y1 + dy - 4, 8, 8);
        }
        if (hitType == ScanHitType.HEART) {
            g2d.setColor(new Color(255, 50, 50, (int)(alpha * 200)));
            int hx = (int)(Math.cos(dir) * len);
            int hy = (int)(Math.sin(dir) * len);
            g2d.fillOval(x1 + hx - 5, y1 + hy - 5, 10, 10);
        }
        if (hitType == ScanHitType.OBSTACLE || hitType == ScanHitType.WALL) {
            g2d.setColor(new Color(255, 255, 255, (int)(alpha * 150)));
            int hx = (int)(Math.cos(dir) * len);
            int hy = (int)(Math.sin(dir) * len);
            g2d.fillOval(x1 + hx - 3, y1 + hy - 3, 6, 6);
        }
    }

    private void drawGrenades(Graphics2D g2d) {
        for (Grenade g : game.getGrenades()) {
            if (g.isExploded()) drawBlastZone(g2d, g);
            if (g.isInFlight()) drawGrenadeInFlight(g2d, g);
        }
    }

    private void drawBlastZone(Graphics2D g2d, Grenade g) {
        int bx = (int) g.getBlastCenterX();
        int by = (int) g.getBlastCenterY();
        int br = (int) g.getBlastRadius();
        float alpha = Math.min(1, g.getExplosionTimer() / 15.0f);
        g2d.setColor(new Color(200, 100, 30, (int)(alpha * 80)));
        g2d.fillOval(bx - br, by - br, br * 2, br * 2);
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(new Color(220, 140, 50, (int)(alpha * 180)));
        g2d.drawOval(bx - br, by - br, br * 2, br * 2);
        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(new Color(220, 140, 50, (int)(alpha * 100)));
        for (int i = 0; i < 16; i++) {
            double a = Math.PI * i / 8;
            int r1 = (int)(br * 0.3);
            int r2 = br - 2;
            g2d.drawLine(bx + (int)(Math.cos(a) * r1), by + (int)(Math.sin(a) * r1),
                bx + (int)(Math.cos(a) * r2), by + (int)(Math.sin(a) * r2));
        }
    }

    private void drawGrenadeInFlight(Graphics2D g2d, Grenade g) {
        int r = (int) Grenade.RADIUS;
        g2d.setColor(g.getColor());
        g2d.fillOval((int) g.getX() - r, (int) g.getY() - r, r * 2, r * 2);
        g2d.setColor(Color.WHITE);
        g2d.fillOval((int) g.getX() - r / 2, (int) g.getY() - r / 2, r, r);
    }

    private void drawHearts(Graphics2D g2d) {
        for (Heart h : game.getHearts()) {
            h.draw(g2d);
        }
    }

    private void drawProjectiles(Graphics2D g2d) {
        for (Projectile p : game.getProjectiles()) {
            g2d.setColor(p.getColor());
            int r = (int) Projectile.RADIUS;
            g2d.fillOval((int) p.getX() - r, (int) p.getY() - r, r * 2, r * 2);
            g2d.setColor(Color.WHITE);
            g2d.fillOval((int) p.getX() - r / 2, (int) p.getY() - r / 2, r, r);
        }
    }

    private void drawRobots(Graphics2D g2d) {
        for (JRobot robot : game.getRobots()) {
            if (!robot.isAlive()) continue;
            int x = (int) robot.getX();
            int y = (int) robot.getY();
            int r = (int) JRobot.RADIUS;
            g2d.setColor(robot.getColor());
            g2d.fillOval(x - r, y - r, r * 2, r * 2);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(x - r, y - r, r * 2, r * 2);
            drawFace(g2d, robot, x, y, r);
            drawHat(g2d, robot, x, y, r);
            double dir = robot.getDirection();
            int lx = (int) (x + Math.cos(dir) * r * 1.3);
            int ly = (int) (y + Math.sin(dir) * r * 1.3);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(x, y, lx, ly);
            String name = robot.getController().getName();
            g2d.setFont(new Font("Monospaced", Font.BOLD, 12));
            g2d.drawString(name, x - 15, y - r - 8);
        }
    }

    private void drawFace(Graphics2D g2d, JRobot robot, int x, int y, int r) {
        String name = robot.getController().getName();
        Color color = robot.getColor();
        int eyeR = Math.max(1, r / 7);
        int ey = y - r / 5;
        int ex1 = x - r / 3;
        int ex2 = x + r / 3;
        g2d.setStroke(new BasicStroke(Math.max(1, r / 8f)));
        g2d.setColor(Color.WHITE);
        switch (name) {
            case "Madbernie" -> drawHappyFace(g2d, x, y, r, eyeR, ex1, ex2, ey);
            case "Krossarn" -> drawAngryFace(g2d, x, y, r, eyeR, ex1, ex2, ey);
            case "Prickskyttarn" -> drawFocusedFace(g2d, x, y, r, eyeR, ex1, ex2, ey);
            case "Spöket" -> drawSpookyFace(g2d, x, y, r, eyeR, ex1, ex2, ey, color);
            default -> drawHappyFace(g2d, x, y, r, eyeR, ex1, ex2, ey);
        }
    }

    private void drawHappyFace(Graphics2D g2d, int x, int y, int r,
        int eyeR, int ex1, int ex2, int ey) {
        g2d.fillOval(ex1 - eyeR, ey - eyeR, eyeR * 2, eyeR * 2);
        g2d.fillOval(ex2 - eyeR, ey - eyeR, eyeR * 2, eyeR * 2);
        g2d.drawArc(x - r / 3, y - 1, r * 2 / 3, r / 2, 0, -180);
    }

    private void drawAngryFace(Graphics2D g2d, int x, int y, int r,
        int eyeR, int ex1, int ex2, int ey) {
        g2d.fillOval(ex1 - eyeR, ey - eyeR, eyeR * 2, eyeR * 2);
        g2d.fillOval(ex2 - eyeR, ey - eyeR, eyeR * 2, eyeR * 2);
        g2d.drawArc(x - r / 3, y, r * 2 / 3, r / 2, 0, 180);
        g2d.setStroke(new BasicStroke(Math.max(1, r / 10f)));
        g2d.drawLine(ex1 - eyeR - 2, ey - eyeR - 3, ex1 + eyeR + 2, ey - r / 3);
        g2d.drawLine(ex2 + eyeR + 2, ey - eyeR - 3, ex2 - eyeR - 2, ey - r / 3);
    }

    private void drawFocusedFace(Graphics2D g2d, int x, int y, int r,
        int eyeR, int ex1, int ex2, int ey) {
        g2d.fillOval(ex1 - eyeR, ey - eyeR, eyeR * 2, eyeR * 2);
        g2d.drawLine(ex2 - eyeR, ey, ex2 + eyeR, ey);
        g2d.setStroke(new BasicStroke(Math.max(1, r / 10f)));
        g2d.drawLine(ex1 - eyeR - 2, ey - eyeR - 2, ex1 + eyeR + 2, ey + eyeR + 2);
        g2d.drawLine(ex1 - eyeR - 2, ey + eyeR + 2, ex1 + eyeR + 2, ey - eyeR - 2);
    }

    private void drawSpookyFace(Graphics2D g2d, int x, int y, int r,
        int eyeR, int ex1, int ex2, int ey, Color color) {
        int bigEye = eyeR * 2;
        g2d.fillOval(ex1 - bigEye, ey - bigEye, bigEye * 2, bigEye * 2);
        g2d.fillOval(ex2 - bigEye, ey - bigEye, bigEye * 2, bigEye * 2);
        g2d.setColor(color);
        int pupil = Math.max(1, eyeR / 2);
        g2d.fillOval(ex1 - pupil, ey - pupil, pupil * 2, pupil * 2);
        g2d.fillOval(ex2 - pupil, ey - pupil, pupil * 2, pupil * 2);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(Math.max(1, r / 10f)));
        g2d.drawOval(x - r / 4, y + r / 6, r / 2, r / 3);
    }

    private void drawHat(Graphics2D g2d, JRobot robot, int x, int y, int r) {
        String name = robot.getController().getName();
        g2d.setColor(robot.getColor());
        switch (name) {
            case "Madbernie" -> drawPartyHat(g2d, x, y, r);
            case "Krossarn" -> drawVikingHelmet(g2d, x, y, r);
            case "Prickskyttarn" -> drawTopHat(g2d, x, y, r);
            case "Spöket" -> drawWitchHat(g2d, x, y, r);
        }
    }

    private void drawPartyHat(Graphics2D g2d, int x, int y, int r) {
        int[] xs = { x, x - r / 2, x + r / 2 };
        int[] ys = { y - r - r * 2 / 3, y - r - 1, y - r - 1 };
        g2d.fillPolygon(xs, ys, 3);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawPolygon(xs, ys, 3);
        g2d.fillOval(x - r / 6, y - r - r * 2 / 3 - r / 5, r / 3, r / 3);
    }

    private void drawVikingHelmet(Graphics2D g2d, int x, int y, int r) {
        g2d.setStroke(new BasicStroke(r / 5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawArc(x - r * 2 / 3, y - r - r / 3, r * 4 / 3, r * 2 / 3, 0, 180);
        g2d.drawLine(x - r * 2 / 3, y - r - 1, x - r - r / 3, y - r / 2);
        g2d.drawLine(x + r * 2 / 3, y - r - 1, x + r + r / 3, y - r / 2);
    }

    private void drawTopHat(Graphics2D g2d, int x, int y, int r) {
        int tw = r * 2 / 3;
        int th = r / 2;
        g2d.fillRect(x - tw / 2, y - r - th - r / 4 - 1, tw, th);
        g2d.setStroke(new BasicStroke(r / 6f));
        g2d.setColor(Color.WHITE);
        g2d.drawRect(x - tw / 2, y - r - th - r / 4 - 1, tw, th);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(x - tw / 2 - 2, y - r - 1, x + tw / 2 + 2, y - r - 1);
    }

    private void drawWitchHat(Graphics2D g2d, int x, int y, int r) {
        int[] xs = { x, x - r / 2, x + r / 2 };
        int[] ys = { y - r - r - r / 4, y - r - 1, y - r - 1 };
        g2d.fillPolygon(xs, ys, 3);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawPolygon(xs, ys, 3);
        g2d.drawLine(x - r / 2 - 2, y - r, x + r / 2 + 2, y - r);
    }

    private void drawHUD(Graphics2D g2d) {
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        int y = 30;
        for (JRobot robot : game.getRobots()) {
            String name = robot.getController().getName();
            String text = name + ": " + (int) robot.getHealth() + "% HP";
            g2d.setColor(robot.getColor());
            g2d.drawString(text, 20, y);
            y += 25;
        }
    }

    private void drawGameOver(Graphics2D g2d) {
        JRobot winner = findWinner();
        if (winner == null) return;

        int w = getWidth();
        int h = getHeight();
        int cx = w / 3;
        int cy = h / 2;
        int bigR = Math.min(w, h) / 5;

        drawGameOverBackground(g2d, w, h);
        drawWinnerRobot(g2d, winner, cx, cy, bigR);
        drawGameOverInfo(g2d, winner, cx + bigR + 50, cy - 90, 32);
    }

    private JRobot findWinner() {
        for (JRobot r : game.getRobots()) {
            if (r.isAlive()) return r;
        }
        return null;
    }

    private void drawGameOverBackground(Graphics2D g2d, int w, int h) {
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRect(0, 0, w, h);
    }

    private void drawWinnerRobot(Graphics2D g2d, JRobot winner, int cx, int cy, int r) {
        g2d.setColor(new Color(255, 255, 200, 40));
        g2d.fillOval(cx - r - 20, cy - r - 20, r * 2 + 40, r * 2 + 40);
        RobotPattern pattern = winner.getPattern();
        if (pattern != null) {
            pattern.draw(g2d, cx, cy, r, winner.getColor());
        } else {
            g2d.setColor(winner.getColor());
            g2d.fillOval(cx - r, cy - r, r * 2, r * 2);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawOval(cx - r, cy - r, r * 2, r * 2);
        }
        drawFace(g2d, winner, cx, cy, r);
        drawHat(g2d, winner, cx, cy, r);
    }

    private void drawGameOverInfo(Graphics2D g2d, JRobot winner, int ix, int iy, int lh) {
        RobotPattern pattern = winner.getPattern();
        g2d.setFont(new Font("Monospaced", Font.BOLD, 36));
        g2d.setColor(Color.WHITE);
        g2d.drawString(winner.getController().getName() + " vann!", ix, iy);
        iy += 50;

        int iw = 360;
        int ih = lh * 8 + 20;
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.fillRect(ix - 10, iy - 10, iw, ih);
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.drawRect(ix - 10, iy - 10, iw, ih);

        drawInfoLine(g2d, "Hälsa", (int) winner.getHealth() + "%", ix, iy,
            winner.getColor());
        drawInfoLine(g2d, "Fart", String.valueOf(Math.round(winner.getSpeed())),
            ix, iy + lh, winner.getColor());
        String typ = winner.getController() instanceof RobotAI ? "AI" : "Användare";
        drawInfoLine(g2d, "Typ", typ, ix, iy + lh * 2, winner.getColor());
        String pname = pattern != null ? pattern.name() : "-";
        drawInfoLine(g2d, "Mönster", pname, ix, iy + lh * 3,
            winner.getColor());

        iy += lh * 4;
        g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Vinster", ix, iy);
        iy += lh;
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
        for (JRobot r : game.getRobots()) {
            int wins = JRobotGame.getWinCounts()
                .getOrDefault(r.getController().getName(), 0);
            drawInfoLine(g2d, r.getController().getName(),
                String.valueOf(wins), ix, iy, r.getColor());
            iy += lh;
        }

        g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString("Tryck R för nytt spel", ix, iy + 10);

        int siy = iy + 50;
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.fillRect(ix - 10, siy - 10, 360, 90);
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.drawRect(ix - 10, siy - 10, 360, 90);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Madbernie", ix, siy);
        siy += 22;
        for (JRobot r : game.getRobots()) {
            if (!(r.getController() instanceof RobotUser)) continue;
            RobotUser u = (RobotUser) r.getController();
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
            g2d.setColor(new Color(220, 220, 220));
            g2d.drawString(u.getStrategyInfo(), ix, siy);
            siy += 18;
            int hp = (int) r.getHealth();
            String betyg = hp > 50 ? "Bra" : hp > 20 ? "Ok" : "D\u00e5lig";
            g2d.drawString("Betyg: " + betyg + " (H\u00e4lsa: " + hp + "%)", ix, siy);
        }
    }

    private void drawInfoLine(Graphics2D g2d, String label, String value,
        int x, int y, Color color) {
        g2d.setColor(new Color(200, 200, 200));
        g2d.drawString(label + ": ", x, y);
        g2d.setColor(color);
        g2d.drawString(value, x + 160, y);
    }

    public void stopGame() {
        if (timer != null && timer.isRunning()) timer.stop();
    }
}
