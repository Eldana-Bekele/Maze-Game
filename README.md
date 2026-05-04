Maze Game — Complete One‑Page MVC Specification
MODEL (Game State & Logic)
Core Data
Maze grid (2D array of walls, paths, exit, power‑ups).
Player position and movement state.
Enemy list (each with position, speed, and simple AI behavior).
Power‑ups (type, position, active duration).
Countdown timer.
Current level number.
Game state:
enum GameState { RUNNING, WON, LOST }
Maze
Top‑down grid representation.
Each level loads a more complex maze:
More branching paths.
More dead ends.
Longer routes to the exit.
Increased structural difficulty.
Maze can be predefined or procedurally generated.
Player
Moves one tile at a time using arrow keys.
Cannot pass through walls.
Can pick up power‑ups.
Affected by speed‑boost power‑up.
Enemies
Store position and speed.
Simple follow AI:
Move in the general direction of the player (up/down/left/right).
If blocked by a wall, try alternate directions.
Speed increases each level, making later levels more intense.
Power‑Ups
Types:
Freeze enemies (stops enemy movement temporarily).
Speed boost (increases player movement speed temporarily).
Spawn randomly in the maze.
Limited number per level.
Each has a duration tracked in the model.
Timer
Countdown timer per level.
Reaching zero triggers a loss unless the player is already on the exit tile.
Win/Loss Conditions
Win: Player reaches the exit before the timer hits zero.
Loss:
Timer reaches zero.
Enemy touches the player.
Level System
Game tracks current level.
When advancing to the next level:
Load a new, more complex maze.
Increase enemy speed.
Reset timer.
Reset player and enemy positions.
Spawn new limited power‑ups.
Level number is displayed when a level is completed.
VIEW (Swing UI)
Main Game Panel
Renders the maze in a top‑down grid.
Draws:
Walls
Paths
Exit tile
Player
Enemies (always visible)
Power‑ups (visible at spawn locations)
Smooth tile‑to‑tile movement animations.
HUD
Visible countdown timer.
Active power‑up indicators (e.g., “Freeze: 2s”).
Level number displayed during gameplay.
Win/Lose Messages
When GameState == LOST:
Display large centered text: “YOU LOSE”
High contrast, overlays the maze.
When GameState == WON:
Display large centered text: “LEVEL: <level number>”
Shown before transitioning to the next level.
Messages are drawn in paintComponent based on the Model’s game state.
CONTROLLER (Input & Game Flow)
Player Input
Arrow keys move the player only when GameState == RUNNING.
Movement checks for wall collisions before updating the model.
Restart Logic
When GameState == LOST:
Pressing R restarts the entire game:
Level resets to 1.
Maze regenerated.
Timer reset.
Player and enemies repositioned.
GameState → RUNNING.
Game Loop
Runs on a Swing Timer or loop. Each tick:
Decrease countdown timer.
Update enemy movement (simple follow logic).
Apply power‑up effects.
Check for collisions (player vs. enemy, player vs. power‑up).
Check win/loss conditions.
Repaint the view.
Power‑Up Handling
When player steps on a power‑up tile:
Activate effect.
Remove power‑up from maze.
Start duration timer.
Freeze stops enemy movement temporarily.
Speed boost increases player movement temporarily.
Level Transition
When the player reaches the exit:
GameState → WON.
View displays “LEVEL: <level number>”.
Controller increments level.
Loads new maze and resets all state.
GameState → RUNNING.