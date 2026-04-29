

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
        drawPlayer(g);
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
                    g.setColor(new Color(0, 0, 150)); // Pac-Man blue walls
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

    private void drawPlayer(Graphics g) {
        int[][] grid = model.getMazeGrid();
        int rows = grid.length;
        int cols = grid[0].length;

        int tileW = getWidth() / cols;
        int tileH = getHeight() / rows;

        g.setColor(Color.ORANGE);
        g.fillOval(
            model.getPlayerCol() * tileW,
            model.getPlayerRow() * tileH,
            tileW,
            tileH
        );
    }

    private void drawHUD(Graphics g) {
        g.setColor(Color.WHITE);

        int time = model.getTimer();
        int minutes = time / 60;
        int seconds = time % 60;

        g.drawString("Time: " + String.format("%d:%02d", minutes, seconds), 10, 20);
        g.drawString("Level: " + model.getLevel(), 10, 40);
    }

    private void drawGameStateOverlay(Graphics g) {
        if (model.getGameState() == GameModel.GameState.LOST) {
            drawCenteredText(g, "YOU LOSE");
            drawCenteredTextSmall(g, "Press R to restart");
        } else if (model.getGameState() == GameModel.GameState.WON) {
            drawCenteredText(g, "LEVEL COMPLETE");
            drawCenteredTextSmall(g, "Press ENTER for next level");
        }
    }

    private void drawCenteredText(Graphics g, String text) {
        g.setColor(Color.RED);
        g.setFont(g.getFont().deriveFont(48f));
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = getHeight() / 2;
        g.drawString(text, x, y);
    }

    private void drawCenteredTextSmall(Graphics g, String text) {
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(24f));
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        int y = (getHeight() / 2) + 40;
        g.drawString(text, x, y);
    }
}
