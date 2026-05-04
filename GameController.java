import javax.swing.*;
import java.awt.event.*;

public class GameController implements KeyListener, ActionListener {

    private final GameModel model;
    private final GameView view;
    private final Timer gameTimer;

    private boolean up, down, left, right;

    public GameController() {
        model = new GameModel();
        view = new GameView(model);

        JFrame frame = new JFrame("Pac-Man Style Maze");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setResizable(false);

        frame.add(view);
        frame.setVisible(true);
        frame.addKeyListener(this);

        gameTimer = new Timer(16, this); // ~60 FPS
        gameTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (model.getGameState() == GameModel.GameState.RUNNING) {
            if (up) model.movePlayer(-1, 0);
            if (down) model.movePlayer(1, 0);
            if (left) model.movePlayer(0, -1);
            if (right) model.movePlayer(0, 1);
        }

        model.updateGameTick();
        view.repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {

        if (model.getGameState() == GameModel.GameState.LOST &&
        e.getKeyCode() == KeyEvent.VK_R) {
        model.loadLevel(model.getLevel());
        return;
    }

        if (model.getGameState() == GameModel.GameState.WON &&
            e.getKeyCode() == KeyEvent.VK_ENTER) {
            model.loadLevel(model.getLevel() + 1);
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> up = true;
            case KeyEvent.VK_DOWN -> down = true;
            case KeyEvent.VK_LEFT -> left = true;
            case KeyEvent.VK_RIGHT -> right = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> up = false;
            case KeyEvent.VK_DOWN -> down = false;
            case KeyEvent.VK_LEFT -> left = false;
            case KeyEvent.VK_RIGHT -> right = false;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameController::new);
    }
}