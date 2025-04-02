## EquinoxGameV3Clone - Technical Documentation

**Version:** 0.4.41b7
**Framework:** Java Swing

### 1. Overview

EquinoxGameV3Clone is a 2D top-down space shooter game developed in Java using the Swing library for graphics and UI. The player controls a ship, shoots enemies that move in waves, collects score and money, navigates through stages, and interacts with cutscenes and a shop system.

### 2. Core Components & Classes

The project is structured into several key classes managing different aspects of the game:

* **`EquinoxGame.java`**:
    * The main entry point of the application (`main` method).
    * Initializes the main `JFrame` (the game window).
    * Creates instances of `EquinoxGameLogic` (the game itself) and `StageManager` (to control flow).
    * Delegates the initial screen setup (starting with a cutscene) to the `StageManager`.

* **`StageManager.java`**:
    * Manages the overall flow and transitions between different game states: Cutscene -> Shop -> Game Loop.
    * Holds references to the main `JFrame`, `EquinoxGameLogic`, `CutscenePanel`, and `ShopPanel`.
    * Loads and manages `CutsceneData` (narration text, character portraits, backgrounds) for different stages, stored in a `HashMap`.
    * Contains methods (`startCutscene`, `showShop`, `startGameLoop`) to swap the currently displayed panel within the `JFrame` and manage the game loop state (start/stop).

* **`EquinoxGameLogic.java`**:
    * The core engine of the game, extending `JPanel` and implementing `ActionListener` (for the game loop timer) and `KeyListener` (for player input).
    * **Game Loop:** Uses a `javax.swing.Timer` (`gameLoop`) to repeatedly call the `actionPerformed` method, which in turn calls `moveGame` and `repaint`.
    * **Rendering:** Overrides `paintComponent` to draw the background and calls the `draw` method. The `draw` method further delegates to specific drawing methods (`drawShip`, `drawEnemies`, `drawPlayerBullets`, etc.) using `Graphics` object.
    * **Entity Management:** Creates and manages `ArrayLists` of game entities (enemies, player bullets, enemy bullets, tactical abilities). Handles enemy spawning logic (`createEnemies`, `createMiniboss`, `createMainboss`), including scaling difficulty based on waves.
    * **Physics & Movement:** Updates entity positions in the `moveGame` method and its sub-methods (`moveEnemies`, `movePlayerBullets`, etc.). Implements player ship movement with acceleration/deceleration and enemy movement patterns.
    * **Collision Detection:** Implements `detectCollision` (axis-aligned bounding box) and uses it in methods like `checkBulletCollisions`, `checkTacticalCollisions`, `checkEnemyBulletCollisions` to handle interactions.
    * **Input Handling:** Implements `keyPressed` and `keyReleased` to handle player movement (A/D keys) and shooting/ability activation (Space, Q, E keys). Includes cooldown logic for abilities.
    * **State Updates:** Updates the `GameState` object (score, money) based on game events (e.g., enemy kills).
    * **Stage/Wave Logic:** Contains `handleStageAndWaveLogic` to check if a wave/stage is complete, trigger boss spawns, advance waves/stages, and interact with `StageManager` to transition between states.

* **`GameState.java` & `Stage.java`**:
    * `GameState`: A simple data class holding runtime game state variables like `score`, `money`, `enemyCount`, `enemySlain`, and a reference to the current `Stage` object. Also includes flags like `gameOver`. Contains a `dropLoot` method for random money drops.
    * `Stage`: A data class holding information about the current stage, including `stageNumber`, `currentWave`, `totalWaves`, and a flag (`specialEnemySpawned`). Used by `EquinoxGameLogic` and `StageManager` to track progression.

* **`Block.java`**:
    * A base class for most visible game objects (player, enemies, bullets).
    * Contains basic properties like `x`, `y`, `width`, `height`, and an `Image`. Includes getters and setters for these properties.
    * Defines subclasses for specific entity types:
        * `ShipUser`: Represents the player's ship.
        * `Enemy`: Base class for enemies, adding movement logic (`move`, `moveDown`) and state (`alive`, `enemyVelocityX`).
        * `FastEnemy`, `ShootingEnemy`: Extend `Enemy` with modified speed or shooting behavior.
        * `SpecialEnemy`, `Miniboss`, `MainBoss`: Extend `Enemy` for bosses, adding hitpoints, unique movement (`moveY`), and shooting capabilities.
        * `Bullet`: Base class for projectiles, adding a `used` flag.
        * `EnemyBullet`, `TacticalQ`, `TacticalE`, `SpecialMove`: Extend `Bullet` for different types of player and enemy projectiles.

* **`CutscenePanel.java`**:
    * A `JPanel` responsible for displaying cutscenes.
    * Shows a character portrait, background image, and narration text within a designated box.
    * Uses `CutsceneData` (provided by `StageManager`) to get the content for the current cutscene.
    * Implements `KeyListener` to advance narration text on `ENTER` key press and transition to the shop via `StageManager` when the cutscene ends.

* **`ShopPanel.java`**:
    * A `JPanel` for the shop/recruitment interface shown between stages.
    * Displays available characters/upgrades as clickable `JButton` components with images, descriptions, costs, and effects.
    * Checks the player's `money` (from `GameState`) before allowing a purchase.
    * Updates the displayed money and disables purchased options.
    * Transitions to the main game loop via `StageManager` after a selection or potentially a "continue" button (though the current logic seems to proceed after any purchase).

### 3. Key Mechanics Implementation

* **Game States:** Managed by `StageManager`, swapping panels (`CutscenePanel`, `ShopPanel`, `EquinoxGameLogic`) in the main `JFrame`.
* **Rendering:** Standard Swing `paintComponent` cycle driven by the `Timer` and `repaint()` calls.
* **Input:** `KeyListener` interface implemented in `EquinoxGameLogic`.
* **Collisions:** AABB (Axis-Aligned Bounding Box) checks between relevant entities (player bullets vs enemies, enemy bullets vs player, tactical abilities vs enemies).
* **Progression:** Wave completion is checked by `enemyCount` reaching zero in `GameState`. Stage progression involves completing all waves, including miniboss and main boss waves.

### 4. Dependencies

* Java Standard Library
* Java Swing (for GUI and 2D graphics)