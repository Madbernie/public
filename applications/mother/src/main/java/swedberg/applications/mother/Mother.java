package swedberg.applications.mother;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.prefs.Preferences;

public class Mother {

    private static final Preferences PREFS = Preferences.userNodeForPackage(Mother.class);
    private static JFrame frame;
    private static int upCount;
    private static int downCount;
    private static int leftCount;
    private static int rightCount;
    private static int prevUpCount;
    private static int prevDownCount;
    private static int prevLeftCount;
    private static int prevRightCount;
    private static char[][] gridLetters;
    private static boolean isDragMode;
    private static int dragSrcRow;
    private static int dragSrcCol;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Mother::createAndShowGui);
    }

    private static void loadState() {
        upCount = PREFS.getInt("upCount", 0);
        downCount = PREFS.getInt("downCount", 0);
        leftCount = PREFS.getInt("leftCount", 0);
        rightCount = PREFS.getInt("rightCount", 0);
        prevUpCount = upCount;
        prevDownCount = downCount;
        prevLeftCount = leftCount;
        prevRightCount = rightCount;

        String s = PREFS.get("gridLetters", null);
        if (s != null && !s.isEmpty()) {
            String[] parts = s.split(",", 3);
            if (parts.length == 3) {
                int r = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);
                String chars = parts[2];
                if (r == 1 + upCount + downCount && c == 1 + leftCount + rightCount
                        && chars.length() == r * c) {
                    gridLetters = new char[r][c];
                    int idx = 0;
                    for (int i = 0; i < r; i++) {
                        for (int j = 0; j < c; j++) {
                            gridLetters[i][j] = chars.charAt(idx++);
                        }
                    }
                }
            }
        }
    }

    private static void saveCounts() {
        PREFS.putInt("upCount", upCount);
        PREFS.putInt("downCount", downCount);
        PREFS.putInt("leftCount", leftCount);
        PREFS.putInt("rightCount", rightCount);
        if (gridLetters != null) {
            int r = gridLetters.length;
            int c = gridLetters[0].length;
            StringBuilder sb = new StringBuilder();
            sb.append(r).append(',').append(c).append(',');
            for (int i = 0; i < r; i++) {
                for (int j = 0; j < c; j++) {
                    sb.append(gridLetters[i][j]);
                }
            }
            PREFS.put("gridLetters", sb.toString());
        }
    }

    private static void savePosition() {
        PREFS.putInt("frameX", frame.getX());
        PREFS.putInt("frameY", frame.getY());
    }

    private static void createAndShowGui() {
        loadState();

        frame = new JFrame("Mother");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                saveCounts();
                savePosition();
            }
        });

        rebuild();

        int savedX = PREFS.getInt("frameX", Integer.MIN_VALUE);
        if (savedX == Integer.MIN_VALUE) {
            frame.setLocationRelativeTo(null);
        } else {
            frame.setLocation(savedX, PREFS.getInt("frameY", 0));
        }
        frame.setVisible(true);
        frame.setAlwaysOnTop(true);
        frame.toFront();
        frame.requestFocus();
        frame.setAlwaysOnTop(false);
    }

    private static void swapLetters(int r1, int c1, int r2, int c2) {
        char temp = gridLetters[r1][c1];
        gridLetters[r1][c1] = gridLetters[r2][c2];
        gridLetters[r2][c2] = temp;
    }

    private static DropTarget createDropTarget(JPanel canvas, int row, int col) {
        canvas.putClientProperty("row", row);
        canvas.putClientProperty("col", col);
        return new DropTarget(canvas, DnDConstants.ACTION_MOVE, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                dtde.acceptDrag(DnDConstants.ACTION_MOVE);
                canvas.setBackground(new Color(200, 230, 255));
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
                dtde.acceptDrag(DnDConstants.ACTION_MOVE);
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                canvas.setBackground(Color.WHITE);
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                canvas.setBackground(Color.WHITE);
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                    String data = (String) dtde.getTransferable()
                            .getTransferData(DataFlavor.stringFlavor);
                    String[] parts = data.split(",");
                    int srcR = Integer.parseInt(parts[0]);
                    int srcC = Integer.parseInt(parts[1]);
                    int dstR = (int) canvas.getClientProperty("row");
                    int dstC = (int) canvas.getClientProperty("col");
                    if (srcR != dstR || srcC != dstC) {
                        swapLetters(srcR, srcC, dstR, dstC);
                        rebuild();
                    }
                    dtde.dropComplete(true);
                } catch (Exception e) {
                    dtde.dropComplete(false);
                }
            }
        }, true);
    }

    private static JPanel createPanel(int row, int col, int rows, int cols) {
        int r = row, c = col;
        var canvas = new JPanel(new GridBagLayout());
        canvas.setPreferredSize(new Dimension(150, 150));
        canvas.setBackground(Color.WHITE);
        canvas.setDropTarget(createDropTarget(canvas, row, col));

        var popup = new JPopupMenu();
        var dndItem = new JMenuItem("DnD");
        dndItem.addActionListener(e -> {
            dragSrcRow = r;
            dragSrcCol = c;
            isDragMode = true;
            frame.setTitle("Mother — DnD: klicka på målpanelen");
            rebuild();
        });
        popup.add(dndItem);

        if (isDragMode && r == dragSrcRow && c == dragSrcCol) {
            canvas.setBackground(new Color(255, 230, 200));
        }

        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isDragMode) {
                    int dr = (int) canvas.getClientProperty("row");
                    int dc = (int) canvas.getClientProperty("col");
                    if (SwingUtilities.isLeftMouseButton(e)
                            && (dragSrcRow != dr || dragSrcCol != dc)) {
                        swapLetters(dragSrcRow, dragSrcCol, dr, dc);
                    }
                    isDragMode = false;
                    frame.setTitle("Mother");
                    rebuild();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    popup.show(canvas, e.getX(), e.getY());
                }
            }
        });

        var letter = new JLabel(String.valueOf(gridLetters[row][col]));
        letter.setFont(new Font("SansSerif", Font.BOLD, 48));
        canvas.add(letter);

        var panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        if (rows == 1 || row == 0) {
            var north = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            var up = new JButton("\u2191");
            up.addActionListener(e -> {
                upCount++;
                frame.setLocation(frame.getX(), frame.getY() - 150);
                savePosition();
                rebuild();
            });
            north.add(up);

            var collapseUp = new JButton("\u2212");
            collapseUp.setVisible(upCount > 0);
            collapseUp.addActionListener(e -> {
                upCount--;
                frame.setLocation(frame.getX(), frame.getY() + 150);
                savePosition();
                rebuild();
            });
            north.add(collapseUp);
            panel.add(north, BorderLayout.NORTH);
        }
        if (rows == 1 || row == rows - 1) {
            var south = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            var down = new JButton("\u2193");
            down.addActionListener(e -> {
                downCount++;
                rebuild();
            });
            south.add(down);
            var collapseDown = new JButton("\u2212");
            collapseDown.setVisible(downCount > 0);
            collapseDown.addActionListener(e -> {
                downCount--;
                rebuild();
            });
            south.add(collapseDown);
            panel.add(south, BorderLayout.SOUTH);
        }
        if (cols == 1 || col == 0) {
            var west = new JPanel(new GridLayout(2, 1));
            var left = new JButton("\u2190");
            left.addActionListener(e -> {
                leftCount++;
                frame.setLocation(frame.getX() - 150, frame.getY());
                savePosition();
                rebuild();
            });
            west.add(left);
            var collapseLeft = new JButton("\u2212");
            collapseLeft.setVisible(leftCount > 0);
            collapseLeft.addActionListener(e -> {
                leftCount--;
                frame.setLocation(frame.getX() + 150, frame.getY());
                savePosition();
                rebuild();
            });
            west.add(collapseLeft);
            panel.add(west, BorderLayout.WEST);
        }
        if (cols == 1 || col == cols - 1) {
            var east = new JPanel(new GridLayout(2, 1));
            var right = new JButton("\u2192");
            right.addActionListener(e -> {
                rightCount++;
                rebuild();
            });
            east.add(right);
            var collapseRight = new JButton("\u2212");
            collapseRight.setVisible(rightCount > 0);
            collapseRight.addActionListener(e -> {
                rightCount--;
                rebuild();
            });
            east.add(collapseRight);
            panel.add(east, BorderLayout.EAST);
        }

        panel.add(canvas, BorderLayout.CENTER);
        return panel;
    }

    private static void rebuild() {
        int newRows = 1 + upCount + downCount;
        int newCols = 1 + leftCount + rightCount;

        int rowOff = upCount - prevUpCount;
        int colOff = leftCount - prevLeftCount;

        char[][] newLetters = new char[newRows][newCols];
        char next = 'A';

        if (gridLetters != null) {
            for (int r = 0; r < gridLetters.length; r++) {
                for (int c = 0; c < gridLetters[0].length; c++) {
                    if (gridLetters[r][c] >= next) {
                        next = (char) (gridLetters[r][c] + 1);
                    }
                }
            }
            for (int r = 0; r < gridLetters.length; r++) {
                for (int c = 0; c < gridLetters[0].length; c++) {
                    int nr = r + rowOff;
                    int nc = c + colOff;
                    if (nr >= 0 && nr < newRows && nc >= 0 && nc < newCols) {
                        newLetters[nr][nc] = gridLetters[r][c];
                    }
                }
            }
        }

        for (int r = 0; r < newRows; r++) {
            for (int c = 0; c < newCols; c++) {
                if (newLetters[r][c] == 0) {
                    newLetters[r][c] = next++;
                }
            }
        }

        gridLetters = newLetters;
        prevUpCount = upCount;
        prevDownCount = downCount;
        prevLeftCount = leftCount;
        prevRightCount = rightCount;

        var content = new JPanel(new GridLayout(newRows, newCols));
        for (int r = 0; r < newRows; r++) {
            for (int c = 0; c < newCols; c++) {
                content.add(createPanel(r, c, newRows, newCols));
            }
        }
        frame.setContentPane(content);
        frame.pack();
        saveCounts();
    }
}
