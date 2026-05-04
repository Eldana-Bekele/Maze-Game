import java.util.*;

public class GameModel {

    public enum GameState { RUNNING, WON, LOST }
    private enum MonsterState { CHASE, PATROL }
    private int[][] mazeGrid;
    private int playerRow, playerCol;
    private List<int[]> monsters = new ArrayList<>();
    private List<Integer> monsterCooldowns = new ArrayList<>();
    private List<int[]> patrolTargets = new ArrayList<>();
    private List<MonsterState> monsterStates = new ArrayList<>();
    private boolean monsterActive = false;
    private int level = 1;
    private int timer;
    private int tickCounter = 0;
    private int moveCooldown = 0;
    private List<int[]> orbs = new ArrayList<>();
    private boolean monstersFrozen = false;
    private int freezeTicks = 0;
    private GameState gameState = GameState.RUNNING;

    public GameModel() { loadLevel(level); }

    public void loadLevel(int level) {
        this.level = level;
        generateMaze();
        resetPlayer();
        resetTimer();
        tickCounter = 0;
        moveCooldown = 0;
        gameState = GameState.RUNNING;
        monsters.clear();
        monsterCooldowns.clear();
        patrolTargets.clear();
        monsterStates.clear();
        orbs.clear();
        monstersFrozen = false;
        freezeTicks = 0;
        monsterActive = level >= 5;
        if (monsterActive) {
            int monsterCount = (level >= 10) ? 3 : 1;
            for (int i = 0; i < monsterCount; i++) {
                spawnMonster();
                monsterCooldowns.add(0);
                monsterStates.add(MonsterState.PATROL);
            }
        }
        if (level >= 10) {
            orbs.add(randomOpenTile());
            orbs.add(randomOpenTile());
        }
    }

    private void activateFreeze() {
        monstersFrozen = true;
        freezeTicks = 300;
    }

    private void spawnMonster() {
        int rows = mazeGrid.length;
        int cols = mazeGrid[0].length;
        for (int r = rows - 2; r > 1; r--) {
            for (int c = cols - 2; c > 1; c--) {
                if (mazeGrid[r][c] == 0) {
                    int distPlayer = Math.abs(r - playerRow) + Math.abs(c - playerCol);
                    if (distPlayer > 10) {
                        monsters.add(new int[]{r, c});
                        patrolTargets.add(randomOpenTile());
                        return;
                    }
                }
            }
        }
        monsters.add(new int[]{rows - 2, cols - 2});
        patrolTargets.add(randomOpenTile());
    }

    private int[] randomOpenTile() {
        while (true) {
            int r = (int)(Math.random() * mazeGrid.length);
            int c = (int)(Math.random() * mazeGrid[0].length);
            if (mazeGrid[r][c] == 0) return new int[]{r, c};
        }
    }

    private void generateMaze() {
        int rows = 15 + level * 2;
        int cols = 20 + level * 2;
        if (rows % 2 == 0) rows++;
        if (cols % 2 == 0) cols++;
        mazeGrid = new int[rows][cols];
        for (int r = 0; r < rows; r++) Arrays.fill(mazeGrid[r], 1);
        carvePath(1, 1);
        addLoops();
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
            if (nr > 0 && nr < mazeGrid.length - 1 && nc > 0 && nc < mazeGrid[0].length - 1 && mazeGrid[nr][nc] == 1) {
                mazeGrid[r + dr[i] / 2][c + dc[i] / 2] = 0;
                carvePath(nr, nc);
            }
        }
    }

    private void addLoops() {
        for (int r = 1; r < mazeGrid.length - 1; r++) {
            for (int c = 1; c < mazeGrid[0].length - 1; c++) {
                if (mazeGrid[r][c] == 1 && Math.random() < 0.05) mazeGrid[r][c] = 0;
            }
        }
    }

    private void resetPlayer() { playerRow = 1; playerCol = 1; }
    private void resetTimer() { timer = 300; }

    public void movePlayer(int dRow, int dCol) {
        if (gameState != GameState.RUNNING) return;
        if (moveCooldown > 0) return;
        int newRow = playerRow + dRow;
        int newCol = playerCol + dCol;
        if (newRow >= 0 && newRow < mazeGrid.length && newCol >= 0 && newCol < mazeGrid[0].length && mazeGrid[newRow][newCol] != 1) {
            playerRow = newRow;
            playerCol = newCol;
            moveCooldown = 5;
        }
    }

    public void updateGameTick() {
        if (gameState != GameState.RUNNING) return;
        tickCounter++;
        if (tickCounter >= 60) { timer--; tickCounter = 0; }
        if (timer <= 0) gameState = GameState.LOST;
        if (moveCooldown > 0) moveCooldown--;
        Iterator<int[]> it = orbs.iterator();
        while (it.hasNext()) {
            int[] o = it.next();
            if (playerRow == o[0] && playerCol == o[1]) { activateFreeze(); it.remove(); }
        }
        if (monstersFrozen) {
            freezeTicks--;
            if (freezeTicks <= 0) monstersFrozen = false;
        }
        if (monsterActive && !monstersFrozen) updateMonsters();
        for (int[] m : monsters) {
            if (playerRow == m[0] && playerCol == m[1]) gameState = GameState.LOST;
        }
        if (mazeGrid[playerRow][playerCol] == 2) gameState = GameState.WON;
    }

    private void updateMonsters() {
        for (int i = 0; i < monsters.size(); i++) {
            int[] m = monsters.get(i);
            if (monsterCooldowns.get(i) > 0) {
                monsterCooldowns.set(i, monsterCooldowns.get(i) - 1);
                continue;
            }
            int[] next;
            if (canSeePlayer(m[0], m[1])) {
                monsterStates.set(i, MonsterState.CHASE);
                next = bfs(m[0], m[1], playerRow, playerCol);
                monsterCooldowns.set(i, 4);
            } else {
                monsterStates.set(i, MonsterState.PATROL);
                int[] target = patrolTargets.get(i);
                if (m[0] == target[0] && m[1] == target[1]) patrolTargets.set(i, randomOpenTile());
                target = patrolTargets.get(i);
                next = bfs(m[0], m[1], target[0], target[1]);
                monsterCooldowns.set(i, 8);
            }
            if (next != null) { m[0] = next[0]; m[1] = next[1]; }
        }
    }

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

    private int[] bfs(int sr, int sc, int tr, int tc) {
        int rows = mazeGrid.length; int cols = mazeGrid[0].length;
        boolean[][] visited = new boolean[rows][cols];
        int[][] pr = new int[rows][cols]; int[][] pc = new int[rows][cols];
        Queue<int[]> q = new LinkedList<>();
        q.add(new int[]{sr, sc}); visited[sr][sc] = true;
        int[] dr = {-1,1,0,0}, dc = {0,0,-1,1};
        while (!q.isEmpty()) {
            int[] cur = q.poll();
            if (cur[0] == tr && cur[1] == tc) break;
            for (int i=0;i<4;i++) {
                int nr = cur[0]+dr[i], nc = cur[1]+dc[i];
                if (nr>=0 && nr<rows && nc>=0 && nc<cols && !visited[nr][nc] && mazeGrid[nr][nc]!=1) {
                    visited[nr][nc]=true; pr[nr][nc]=cur[0]; pc[nr][nc]=cur[1]; q.add(new int[]{nr,nc});
                }
            }
        }
        if (!visited[tr][tc]) return null;
        int r=tr,c=tc;
        while (!(pr[r][c]==sr && pc[r][c]==sc)) {
            int trr=pr[r][c]; int tcc=pc[r][c]; r=trr; c=tcc;
        }
        return new int[]{r,c};
    }

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