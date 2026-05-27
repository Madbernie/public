package swedberg.applications.jrobot;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class JRobotGUI {
    private static JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JRobotGame game = new JRobotGame();
            JRobotPanel panel = new JRobotPanel(game);
            frame = new JFrame("JRobot - Robot Fight!");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
            frame.setAlwaysOnTop(true);
            frame.toFront();
            frame.requestFocus();
        panel.requestFocusInWindow();
        frame.setAlwaysOnTop(true);
        frame.toFront();
        frame.requestFocus();
        frame.setAlwaysOnTop(false);
            frame.setAlwaysOnTop(false);
        });
    }

    static void startNewGame(JRobotPanel oldPanel) {
        oldPanel.stopGame();
        JRobotGame game = new JRobotGame();
        JRobotPanel panel = new JRobotPanel(game);
        oldPanel.setVisible(false);
        frame.remove(oldPanel);
        frame.add(panel);
        frame.revalidate();
        frame.repaint();
        panel.requestFocusInWindow();
    }
}
