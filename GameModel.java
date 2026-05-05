import java.util.*;

// GameModel handles all core game logic and state (MVC: Model)
public class GameModel {

    // Represents the overall game state
    public enum GameState { RUNNING, WON, LOST }

    // Internal states for monster behavior (chasing player or patrolling)
    private enum MonsterState { CHASE, PATROL }

    // 2D maze grid: 0 = path, 1 = wall, 2 = exit
    private int[][] mazeGrid;

    // Player position in the maze
    private int playerRow, playerCol;

    // Lists storing monster positions, movement cooldowns, patrol targets, and behavior states
    private List<int[]> monsters = new ArrayList<>();
    private List<Integer> monsterCooldowns = new ArrayList<>();
    private List<int[]> patrolTargets = new ArrayList<>();
    private List<MonsterState> monsterStates = new ArrayList<>();

    // Determines if monsters are active based on level
    private boolean monsterActive = false;

    // Current level number
    private int level = 1;

    // Countdown timer for the level
    private int timer;

    // Used to track time progression (ticks per second)
    private int tickCounter = 0;

    // Controls how fast the player can move
    private int moveCooldown = 0;

    // List of freeze power-up positions
    private List<int[]> orbs = new ArrayList<>();

    // Freeze effect state
    private boolean monstersFrozen = false;
    private int freezeTicks = 0;

    // Current game state (running, won, or lost)
    private GameState gameState = GameState.RUNNING;

    // Constructor: initializes the first level
    public GameModel() { loadLevel(level); }

    // Loads a level and resets all game elements
    public void loadLevel(int level) {
        this.level = level;

        generateMaze();   // create new maze
        resetPlayer();    // place player at start
        resetTimer();     // reset timer

        tickCounter = 0;
        moveCooldown = 0;
        gameState = GameState.RUNNING;

        // Clear all previous entities
        monsters.clear();
        monsterCooldowns.clear();
        patrolTargets.clear();
        monsterStates.clear();
        orbs.clear();

        monstersFrozen = false;
        freezeTicks = 0;

        // Monsters become active starting at level 5
        monsterActive = level >= 5;

        if (monsterActive) {
            // Increase number of monsters at higher levels
            int monsterCount = (level >= 10) ? 3 : 1;

            for (int i = 0; i < monsterCount; i++) {
                spawnMonster();
                monsterCooldowns.add(0);
                monsterStates.add(MonsterState.PATROL);
            }
        }

        // Spawn freeze power-ups at higher levels
        if (level >= 10) {
            orbs.add(randomOpenTile());
            orbs.add(randomOpenTile());
        }
    }

    // Activates freeze power-up (stops monsters temporarily)
    private void activateFreeze() {
        monstersFrozen = true;
        freezeTicks = 300;
    }

    // Spawns a monster far away from the player
    private void spawnMonster() {
        int rows = mazeGrid.length;
        int cols = mazeGrid[0].length;

        for (int r = rows - 2; r > 1; r--) {
            for (int c = cols - 2; c > 1; c--) {

                if (mazeGrid[r][c] == 0) {
                    int distPlayer = Math.abs(r - playerRow) + Math.abs(c - playerCol);

                    // Ensure monster spawns far from player
                    if (distPlayer > 10) {
                        monsters.add(new int[]{r, c});
                        patrolTargets.add(randomOpenTile());
                        return;
                    }
                }
            }
        }

        // Fallback spawn position if no far tile found
        monsters.add(new int[]{rows - 2, cols - 2});
        patrolTargets.add(randomOpenTile());
    }

    // Returns a random open tile (path) in the maze
    private int[] randomOpenTile() {
        while (true) {
            int r = (int)(Math.random() * mazeGrid.length);
            int c = (int)(Math.random() * mazeGrid[0].length);

            if (mazeGrid[r][c] == 0) return new int[]{r, c};
        }
    }

    // Generates a procedural maze that increases in size with level
    private void generateMaze() {
        int rows = 15 + level * 2;
        int cols = 20 + level * 2;

        // Ensure dimensions are odd for proper maze generation
        if (rows % 2 == 0) rows++;
        if (cols % 2 == 0) cols++;

        mazeGrid = new int[rows][cols];

        // Fill grid with walls
        for (int r = 0; r < rows; r++) Arrays.fill(mazeGrid[r], 1);

        // Carve out paths
        carvePath(1, 1);

        // Add random loops for complexity
        addLoops();

        // Set exit tile
        mazeGrid[rows - 2][cols - 2] = 2;
    }

    // Recursive backtracking algorithm to carve maze paths
    private void carvePath(int r, int c) {
        int[] dr = {-2, 2, 0, 0};
        int[] dc = {0, 0, -2, 2};

        mazeGrid[r][c] = 0;

        Integer[] dirs = {0, 1, 2, 3};
        Collections.shuffle(Arrays.asList(dirs)); // randomize directions

        for (int i : dirs) {
            int nr = r + dr[i];
            int nc = c + dc[i];

            if (nr > 0 && nr < mazeGrid.length - 1 &&
                nc > 0 && nc < mazeGrid[0].length - 1 &&
                mazeGrid[nr][nc] == 1) {

                // Remove wall between cells
                mazeGrid[r + dr[i] / 2][c + dc[i] / 2] = 0;
                carvePath(nr, nc);
            }
        }
    }

    // Randomly removes some walls to create loops in the maze
    private void addLoops() {
        for (int r = 1; r < mazeGrid.length - 1; r++) {
            for (int c = 1; c < mazeGrid[0].length - 1; c++) {

                if (mazeGrid[r][c] == 1 && Math.random() < 0.05) {
                    mazeGrid[r][c] = 0;
                }
            }
        }
    }

    // Resets player to starting position
    private void resetPlayer() { playerRow = 1; playerCol = 1; }

    // Resets level timer
    private void resetTimer() { timer = 300; }

    // Moves player if valid (not into a wall) and cooldown allows
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
            moveCooldown = 5;
        }
    }

    // Main game update loop, called every frame
    public void updateGameTick() {
        if (gameState != GameState.RUNNING) return;

        tickCounter++;

        // Decrease timer every second
        if (tickCounter >= 60) { timer--; tickCounter = 0; }

        // Lose if time runs out
        if (timer <= 0) gameState = GameState.LOST;

        if (moveCooldown > 0) moveCooldown--;

        // Check if player collects a power-up
        Iterator<int[]> it = orbs.iterator();
        while (it.hasNext()) {
            int[] o = it.next();

            if (playerRow == o[0] && playerCol == o[1]) {
                activateFreeze();
                it.remove();
            }
        }

        // Handle freeze duration
        if (monstersFrozen) {
            freezeTicks--;
            if (freezeTicks <= 0) monstersFrozen = false;
        }

        // Update monsters if active and not frozen
        if (monsterActive && !monstersFrozen) updateMonsters();

        // Check collision with monsters
        for (int[] m : monsters) {
            if (playerRow == m[0] && playerCol == m[1]) gameState = GameState.LOST;
        }

        // Check win condition (reaching exit)
        if (mazeGrid[playerRow][playerCol] == 2) gameState = GameState.WON;
    }

    // Updates monster behavior each tick
    private void updateMonsters() {
        for (int i = 0; i < monsters.size(); i++) {
            int[] m = monsters.get(i);

            // Handle movement cooldown
            if (monsterCooldowns.get(i) > 0) {
                monsterCooldowns.set(i, monsterCooldowns.get(i) - 1);
                continue;
            }

            int[] next;

            // If monster can see player, chase them
            if (canSeePlayer(m[0], m[1])) {
                monsterStates.set(i, MonsterState.CHASE);
                next = bfs(m[0], m[1], playerRow, playerCol);
                monsterCooldowns.set(i, 4);

            } else {
                // Otherwise patrol toward a random target
                monsterStates.set(i, MonsterState.PATROL);

                int[] target = patrolTargets.get(i);

                if (m[0] == target[0] && m[1] == target[1])
                    patrolTargets.set(i, randomOpenTile());

                target = patrolTargets.get(i);
                next = bfs(m[0], m[1], target[0], target[1]);
                monsterCooldowns.set(i, 8);
            }

            if (next != null) {
                m[0] = next[0];
                m[1] = next[1];
            }
        }
    }

    // Checks if monster has direct line-of-sight to player (row or column)
    private boolean canSeePlayer(int r, int c) {

        if (r == playerRow) {
            for (int i = Math.min(c, playerCol); i <= Math.max(c, playerCol); i++)
                if (mazeGrid[r][i] == 1) return false;
            return true;
        }

        if (c == playerCol) {
            for (int i = Math.min(r, playerRow); i <= Math.max(r, playerRow); i++)
                if (mazeGrid[i][c] == 1) return false;
            return true;
        }

        return false;
    }

    // Breadth-First Search to find shortest path to a target
    private int[] bfs(int sr, int sc, int tr, int tc) {
        int rows = mazeGrid.length; int cols = mazeGrid[0].length;

        boolean[][] visited = new boolean[rows][cols];
        int[][] pr = new int[rows][cols];
        int[][] pc = new int[rows][cols];

        Queue<int[]> q = new LinkedList<>();
        q.add(new int[]{sr, sc});
        visited[sr][sc] = true;

        int[] dr = {-1,1,0,0}, dc = {0,0,-1,1};

        while (!q.isEmpty()) {
            int[] cur = q.poll();

            if (cur[0] == tr && cur[1] == tc) break;

            for (int i=0;i<4;i++) {
                int nr = cur[0]+dr[i], nc = cur[1]+dc[i];

                if (nr>=0 && nr<rows && nc>=0 && nc<cols &&
                    !visited[nr][nc] && mazeGrid[nr][nc]!=1) {

                    visited[nr][nc]=true;
                    pr[nr][nc]=cur[0];
                    pc[nr][nc]=cur[1];

                    q.add(new int[]{nr,nc});
                }
            }
        }

        if (!visited[tr][tc]) return null;

        int r=tr,c=tc;

        // Backtrack to find next move
        while (!(pr[r][c]==sr && pc[r][c]==sc)) {
            int trr=pr[r][c];
            int tcc=pc[r][c];
            r=trr;
            c=tcc;
        }

        return new int[]{r,c};
    }

    // Getters for View access
    public int[][] getMazeGrid() { return mazeGrid; }
    public int getPlayerRow() { return playerRow; }
    public int getPlayerCol() { return playerCol; }
    public List<int[]> getMonsters() { return monsters; }
    public List<int[]> getOrbs() { return orbs; }
    public boolean isMonsterActive() { return monsterActive; }
    public boolean isMonstersFrozen() { return monstersFrozen; }
    public int getFreezeTicks() { return freezeTicks; }
    public int getLevel() { return level; }
    public int getTimer() { return timer; }
    public GameState getGameState() { return gameState; }
}