import java.util.*;

public class GameModel {

    public enum GameState { RUNNING, WON, LOST }

    private int[][] mazeGrid;
    private int playerRow, playerCol;

    private int level = 1;
    private int timer;
    private int tickCounter = 0;

    private int moveCooldown = 0; // controls speed

    private GameState gameState = GameState.RUNNING;

    public GameModel() {
        loadLevel(level);
    }

    public void loadLevel(int level) {
        this.level = level;
        generateMaze();
        resetPlayer();
        resetTimer();
        tickCounter = 0;
        gameState = GameState.RUNNING;
    }

    // ---------------------------
    // PROCEDURAL MAZE
    // ---------------------------
    private void generateMaze() {
        int rows = 15 + level * 2;
        int cols = 20 + level * 2;

        if (rows % 2 == 0) rows++;
        if (cols % 2 == 0) cols++;

        mazeGrid = new int[rows][cols];

        for (int r = 0; r < rows; r++)
            Arrays.fill(mazeGrid[r], 1);

        carvePath(1, 1);

        mazeGrid[rows - 2][cols - 2] = 2;
    }

    private void carvePath(int r, int c) {
        int[] dr = {-2, 2, 0, 0};
        int[] dc = {0, 0, -2, 2};

        mazeGrid[r][c] = 0;

        Integer[] dirs = {0, 1, 2, 3};
        Collections.shuffle(Arrays.asList(dirs));

        for (int i : dirs) {
            int nr = r + dr[i];
            int nc = c + dc[i];

            if (nr > 0 && nr < mazeGrid.length - 1 &&
                nc > 0 && nc < mazeGrid[0].length - 1 &&
                mazeGrid[nr][nc] == 1) {

                mazeGrid[r + dr[i] / 2][c + dc[i] / 2] = 0;
                carvePath(nr, nc);
            }
        }
    }

    private void resetPlayer() {
        playerRow = 1;
        playerCol = 1;
    }

    private void resetTimer() {
        timer = 300; // 5 minutes
    }

    public void updateGameTick() {
        if (gameState != GameState.RUNNING) return;

        // Timer (60 FPS → 60 ticks = 1 sec)
        tickCounter++;
        if (tickCounter >= 60) {
            timer--;
            tickCounter = 0;
        }

        if (timer <= 0) gameState = GameState.LOST;

        // Movement cooldown
        if (moveCooldown > 0) moveCooldown--;

        if (mazeGrid[playerRow][playerCol] == 2)
            gameState = GameState.WON;
    }

    public void movePlayer(int dRow, int dCol) {
        if (gameState != GameState.RUNNING) return;

        if (moveCooldown > 0) return;

        int newRow = playerRow + dRow;
        int newCol = playerCol + dCol;

        if (newRow >= 0 && newRow < mazeGrid.length &&
            newCol >= 0 && newCol < mazeGrid[0].length &&
            mazeGrid[newRow][newCol] != 1) {

            playerRow = newRow;
            playerCol = newCol;
            moveCooldown = 5; // tweak this (lower = faster)
        }
    }

    public int[][] getMazeGrid() { return mazeGrid; }
    public int getPlayerRow() { return playerRow; }
    public int getPlayerCol() { return playerCol; }
    public int getLevel() { return level; }
    public int getTimer() { return timer; }
    public GameState getGameState() { return gameState; }
}