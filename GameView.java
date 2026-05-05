import javax.swing.*;
import java.awt.*;
import java.util.Random;

// GameView handles all rendering (MVC: View)
// Responsible for drawing the maze, player, enemies, HUD, and visual effects
public class GameView extends JPanel {

    // Reference to the game model (data source)
    private final GameModel model;

    // Random object (not heavily used but available)
    private final Random rand = new Random();

    // Used for snow animation during freeze effect
    private float snowOffset = 0;
    private long lastTime = System.currentTimeMillis();

    // Constructor: initializes view with model and sets background color
    public GameView(GameModel model) {
        this.model = model;
        setBackground(new Color(5, 5, 15)); // Deep Cyberpunk Blue-Black
    }

    // Main rendering method called by Swing
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        
        // Enable anti-aliasing for smoother visuals
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw all game elements
        drawMaze(g2);
        drawOrbs(g2);      
        drawPlayer(g2);
        drawMonster(g2);
        
        // Draw freeze visual effect if active
        if (model.isMonstersFrozen()) {
            drawFreezeEffect(g2);
        }

        // Draw HUD and overlays
        drawAestheticHUD(g2);
        drawGameStateOverlay(g2);
    }

    // Draws the maze including walls and exit tile
    private void drawMaze(Graphics2D g) {
        int[][] grid = model.getMazeGrid();
        int rows = grid.length;
        int cols = grid[0].length;

        // Calculate tile size based on panel dimensions
        int tileW = getWidth() / cols;
        int tileH = getHeight() / rows;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {

                if (grid[r][c] == 1) {
                    // Draw walls with neon glow effect
                    g.setColor(new Color(10, 10, 25));
                    g.fillRect(c * tileW, r * tileH, tileW, tileH);
                    
                    g.setStroke(new BasicStroke(2));
                    g.setColor(new Color(0, 255, 255, 80)); // outer glow
                    g.drawRect(c * tileW + 1, r * tileH + 1, tileW - 2, tileH - 2);

                    g.setColor(new Color(0, 255, 255)); // inner bright line
                    g.drawRect(c * tileW + 2, r * tileH + 2, tileW - 4, tileH - 4);

                } else if (grid[r][c] == 2) {
                    // Draw exit tile
                    g.setColor(new Color(50, 255, 50, 100));
                    g.fillRect(c * tileW, r * tileH, tileW, tileH);

                    g.setColor(new Color(50, 255, 50));
                    g.drawRect(c * tileW, r * tileH, tileW, tileH);
                }
            }
        }
    }

    // Draws power-up orbs
    private void drawOrbs(Graphics2D g) {
        if (model.getOrbs() == null) return;

        int tileW = getWidth() / model.getMazeGrid()[0].length;
        int tileH = getHeight() / model.getMazeGrid().length;

        for (int[] o : model.getOrbs()) {
            // Outer glow
            g.setColor(new Color(0, 255, 255, 100));
            g.fillOval(o[1] * tileW - 2, o[0] * tileH - 2, tileW + 4, tileH + 4);

            // Inner core
            g.setColor(Color.WHITE);
            g.fillOval(o[1] * tileW + tileW/4, o[0] * tileH + tileH/4, tileW/2, tileH/2);
        }
    }

    // Draws the player with glow effect
    private void drawPlayer(Graphics2D g) {
        int tileW = getWidth() / model.getMazeGrid()[0].length;
        int tileH = getHeight() / model.getMazeGrid().length;

        int x = model.getPlayerCol() * tileW;
        int y = model.getPlayerRow() * tileH;

        // Outer glow
        g.setColor(new Color(255, 150, 0, 150));
        g.fillOval(x - 2, y - 2, tileW + 4, tileH + 4);

        // Inner core
        g.setColor(new Color(255, 200, 50));
        g.fillOval(x + 2, y + 2, tileW - 4, tileH - 4);
    }

    // Draws all monsters
    private void drawMonster(Graphics2D g) {
        if (!model.isMonsterActive()) return;

        int tileW = getWidth() / model.getMazeGrid()[0].length;
        int tileH = getHeight() / model.getMazeGrid().length;

        for (int[] m : model.getMonsters()) {
            int mx = m[1] * tileW;
            int my = m[0] * tileH;

            // Outer glow
            g.setColor(new Color(255, 0, 100, 150));
            g.fillOval(mx - 1, my - 1, tileW + 2, tileH + 2);

            // Inner core
            g.setColor(new Color(255, 20, 50));
            g.fillOval(mx + 3, my + 3, tileW - 6, tileH - 6);
        }
    }

    // Draws visual effect when freeze power-up is active
    private void drawFreezeEffect(Graphics2D g) {

        // Transparent blue overlay
        g.setColor(new Color(0, 150, 255, 30));
        g.fillRect(0, 0, getWidth(), getHeight());

        // Snow particle effect
        g.setColor(Color.WHITE);

        long now = System.currentTimeMillis();

        // Slowly move snow downward over time
        snowOffset += (now - lastTime) * 0.02f;
        lastTime = now;

        // Seeded random ensures consistent snow positions
        Random seededRand = new Random(1234);

        for (int i = 0; i < 80; i++) {
            int x = seededRand.nextInt(getWidth());
            int yBase = seededRand.nextInt(getHeight());

            int y = (int)(yBase + snowOffset) % getHeight();

            g.fillOval(x, y, 2, 2);
        }
    }

    // Draws the HUD (timer, level, and freeze status)
    private void drawAestheticHUD(Graphics2D g) {

        // Top bar background
        g.setColor(new Color(0, 0, 20, 180));
        g.fillRect(0, 0, getWidth(), 60);

        // Bottom border line
        g.setColor(new Color(0, 255, 255, 100));
        g.drawLine(0, 60, getWidth(), 60);

        g.setFont(new Font("Monospaced", Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();

        // Format timer
        int time = model.getTimer();
        String timeStr = "Time: " + String.format("%d:%02d", time / 60, time % 60);
        String levelStr = "Level: " + model.getLevel();

        // Draw level (left)
        drawGlowText(g, levelStr, 25, 38, new Color(0, 255, 150));

        // Draw timer (center)
        int centerX = (getWidth() - fm.stringWidth(timeStr)) / 2;
        drawGlowText(g, timeStr, centerX, 38, new Color(0, 200, 255));

        // Draw freeze timer (right)
        if (model.isMonstersFrozen()) {
            String freezeStr = "FREEZE: " + (model.getFreezeTicks() / 60) + "s";
            int rightX = getWidth() - fm.stringWidth(freezeStr) - 25;
            drawGlowText(g, freezeStr, rightX, 38, new Color(255, 0, 200));
        }
    }

    // Helper method to draw glowing text effect
    private void drawGlowText(Graphics2D g, String text, int x, int y, Color c) {
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 80));
        g.drawString(text, x + 1, y + 1);

        g.setColor(c);
        g.drawString(text, x, y);
    }

    // Displays win/lose overlay messages
    private void drawGameStateOverlay(Graphics2D g) {

        if (model.getGameState() == GameModel.GameState.LOST) {
            drawCenteredOverlay(g, "YOU LOSE", "Press R to restart", Color.RED);

        } else if (model.getGameState() == GameModel.GameState.WON) {
            drawCenteredOverlay(g, "LEVEL COMPLETE", "Press ENTER", new Color(0, 255, 100));
        }
    }

    // Draws centered overlay with main and sub text
    private void drawCenteredOverlay(Graphics2D g, String main, String sub, Color c) {

        // Dark background overlay
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, getHeight()/2 - 80, getWidth(), 160);

        // Main text
        g.setFont(new Font("Monospaced", Font.BOLD, 50));
        FontMetrics fm1 = g.getFontMetrics();

        drawGlowText(g, main,
                (getWidth() - fm1.stringWidth(main))/2,
                getHeight()/2,
                c);

        // Sub text
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        FontMetrics fm2 = g.getFontMetrics();

        drawGlowText(g, sub,
                (getWidth() - fm2.stringWidth(sub))/2,
                getHeight()/2 + 45,
                Color.WHITE);
    }
}