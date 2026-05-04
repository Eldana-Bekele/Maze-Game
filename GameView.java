import javax.swing.*;
import java.awt.*;

public class GameView extends JPanel {

    private final GameModel model;

    public GameView(GameModel model) {
        this.model = model;
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawMaze(g);
        drawOrbs(g);      // NEW
        drawPlayer(g);
        drawMonster(g);
        drawHUD(g);
        drawGameStateOverlay(g);
    }

    private void drawMaze(Graphics g) {
        int[][] grid = model.getMazeGrid();
        int rows = grid.length;
        int cols = grid[0].length;

        int tileW = getWidth() / cols;
        int tileH = getHeight() / rows;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                if (grid[r][c] == 1) {
                    g.setColor(new Color(0, 0, 150));
                    g.fillRect(c * tileW, r * tileH, tileW, tileH);

                } else if (grid[r][c] == 2) {
                    g.setColor(Color.YELLOW);
                    g.fillRect(c * tileW, r * tileH, tileW, tileH);

                } else {
                    g.setColor(Color.BLACK);
                    g.fillRect(c * tileW, r * tileH, tileW, tileH);
                }
            }
        }
    }

    // NEW ORBS
    private void drawOrbs(Graphics g) {
        if (model.getOrbs() == null) return;

        int[][] grid = model.getMazeGrid();
        int tileW = getWidth() / grid[0].length;
        int tileH = getHeight() / grid.length;

        g.setColor(Color.CYAN);

        for (int[] o : model.getOrbs()) {
            g.fillOval(
                o[1] * tileW,
                o[0] * tileH,
                tileW,
                tileH
            );
        }
    }

    private void drawPlayer(Graphics g) {
        int[][] grid = model.getMazeGrid();
        int tileW = getWidth() / grid[0].length;
        int tileH = getHeight() / grid.length;

        g.setColor(Color.ORANGE);
        g.fillOval(
            model.getPlayerCol() * tileW,
            model.getPlayerRow() * tileH,
            tileW,
            tileH
        );
    }

    private void drawMonster(Graphics g) {
        if (!model.isMonsterActive()) return;

        int[][] grid = model.getMazeGrid();
        int tileW = getWidth() / grid[0].length;
        int tileH = getHeight() / grid.length;

        g.setColor(Color.RED);

        for (int[] m : model.getMonsters()) {
            g.fillOval(
                m[1] * tileW,
                m[0] * tileH,
                tileW,
                tileH
            );
        }
    }

    private void drawHUD(Graphics g) {
        g.setColor(Color.WHITE);

        int time = model.getTimer();
        int min = time / 60;
        int sec = time % 60;

        g.drawString("Time: " + String.format("%d:%02d", min, sec), 10, 20);
        g.drawString("Level: " + model.getLevel(), 10, 40);

        // NEW FREEZE TIMER
        if (model.isMonstersFrozen()) {
            g.setColor(Color.CYAN);
            g.drawString(
                "FREEZE: " + (model.getFreezeTicks() / 60) + "s",
                10, 60
            );
        }
    }

    private void drawGameStateOverlay(Graphics g) {
        if (model.getGameState() == GameModel.GameState.LOST) {
            drawText(g, "YOU LOSE", Color.RED, 48, -20);
            drawText(g, "Press R to restart", Color.WHITE, 24, 40);
        } else if (model.getGameState() == GameModel.GameState.WON) {
            drawText(g, "LEVEL COMPLETE", Color.GREEN, 48, -20);
            drawText(g, "Press ENTER", Color.WHITE, 24, 40);
        }
    }

    private void drawText(Graphics g, String text, Color color, int size, int yOffset) {
        g.setColor(color);
        g.setFont(g.getFont().deriveFont((float) size));
        FontMetrics fm = g.getFontMetrics();

        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = getHeight() / 2 + yOffset;

        g.drawString(text, x, y);
    }
}