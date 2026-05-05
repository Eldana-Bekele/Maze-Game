import javax.swing.*;
import java.awt.event.*;

// GameController handles input and game flow (MVC: Controller)
// Connects the Model and View, processes user input, and runs the game loop
public class GameController implements KeyListener, ActionListener {

    // Reference to game data (Model)
    private final GameModel model;

    // Reference to rendering system (View)
    private final GameView view;

    // Swing timer used for the game loop (~60 frames per second)
    private final Timer gameTimer;

    // Tracks which movement keys are currently held down
    private boolean up, down, left, right;

    // Constructor: sets up the game window and starts the game loop
    public GameController() {
        model = new GameModel();
        view = new GameView(model);

        // Create main game window
        JFrame frame = new JFrame("Pac-Man Style Maze");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Make window full screen and non-resizable
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setResizable(false);

        // Add game view to window
        frame.add(view);
        frame.setVisible(true);

        // Listen for keyboard input
        frame.addKeyListener(this);

        // Create and start game loop timer (~60 FPS)
        gameTimer = new Timer(16, this);
        gameTimer.start();
    }

    // Game loop: runs every frame (~60 times per second)
    @Override
    public void actionPerformed(ActionEvent e) {

        // Only allow movement if game is running
        if (model.getGameState() == GameModel.GameState.RUNNING) {

            // Move player based on keys currently pressed
            if (up) model.movePlayer(-1, 0);
            if (down) model.movePlayer(1, 0);
            if (left) model.movePlayer(0, -1);
            if (right) model.movePlayer(0, 1);
        }

        // Update game logic (timer, enemies, collisions, etc.)
        model.updateGameTick();

        // Redraw the screen
        view.repaint();
    }

    // Handles key press events
    @Override
    public void keyPressed(KeyEvent e) {

        // Restart current level if player lost and presses R
        if (model.getGameState() == GameModel.GameState.LOST &&
        e.getKeyCode() == KeyEvent.VK_R) {
        model.loadLevel(model.getLevel());
        return;
    }

        // Advance to next level if player won and presses Enter
        if (model.getGameState() == GameModel.GameState.WON &&
            e.getKeyCode() == KeyEvent.VK_ENTER) {
            model.loadLevel(model.getLevel() + 1);
            return;
        }

        // Track movement key presses
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> up = true;
            case KeyEvent.VK_DOWN -> down = true;
            case KeyEvent.VK_LEFT -> left = true;
            case KeyEvent.VK_RIGHT -> right = true;
        }
    }

    // Handles key release events (stops movement)
    @Override
    public void keyReleased(KeyEvent e) {

        // Reset movement flags when keys are released
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> up = false;
            case KeyEvent.VK_DOWN -> down = false;
            case KeyEvent.VK_LEFT -> left = false;
            case KeyEvent.VK_RIGHT -> right = false;
        }
    }

    // Required method for KeyListener (not used here)
    @Override public void keyTyped(KeyEvent e) {}

    // Entry point of the program
    public static void main(String[] args) {

        // Ensures GUI runs on Swing event dispatch thread
        SwingUtilities.invokeLater(GameController::new);
    }
}