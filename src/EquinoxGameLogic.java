import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class EquinoxGameLogic extends JPanel implements ActionListener, KeyListener {

    // INIT
    private GameState gameState;


    // BOARD
    int tileSize = 32;
    int rows = 24;
    int columns = 24;
    int boardWidth = tileSize * columns;
    int boardHeight = tileSize * rows;

    // Image
    Image shipImg;
    Image enemyImgVar1;
    Image enemyImgVar2;
    Image enemyImgVar3;
    Image world1BG;
    Image laserBlue;
    Image enemyBulletImg;

    Image mainbossImgvar1;
    Image minibossImgvar1;
    ArrayList<Image> enemyImgArray;
    ArrayList<Image> specialEnemyImgArray;
    // Game Settings


    long remainingCooldown;
    long remainingCooldownE;

    // Ship
    int shipWidth = tileSize * 2; // 64px
    int shipHeight = tileSize; // 32px
    int shipX = tileSize * columns / 2 - tileSize;
    int shipY = boardHeight - tileSize * 2;
    int shipVelocityX = 0; // Initial velocity
    int shipAcceleration = 2; // Acceleration rate
    int shipMaxSpeed = 8; // Maximum speed
    int shipDeceleration = 1; // Deceleration rate

    ShipUser ship;

    // Enemies
    ArrayList<Enemy> enemyArray;
    int enemyWidth = tileSize * 2;
    int enemyHeight = tileSize;
    int enemyX = 0;     //X START SPAWN
    int enemyY = 0;     //Y START SPAWN
    int enemyVelocityX = 1;
    int enemyRows = 5;
    int enemyColumns = 5;

    // Bullets
    ArrayList<Bullet> bulletArray;
    int bulletWidth = tileSize / 8; // Bullet size width
    int bulletHeight = tileSize / 2;
    int bulletVelocityY = -20; // Bullet movespeed
    // TacticalQ
    ArrayList<TacticalQ> tacticalArray;
    int tacticalqWidth = tileSize / 4; // Bullet size width
    int tacticalqHeight = tileSize * 10;
    int tacticalqVelocityY = -100; // Bullet movespeed
    long lastTacticalQUseTime; // Last tactical use time
    long tacticalQCooldown = 2500; // Tactical Q Cooldown in ms
    //TacticalE
    ArrayList<TacticalE> tacticalEArray;
    int tacticaleWidth = tileSize; // Smaller projectile
    int tacticaleHeight = tileSize / 8;
    int tacticaleVelocityY = -15;
    long lastTacticalEUseTime;
    long tacticalECooldown = 0; //Tactical E Cooldown in ms
    //Enemy Bullets
    ArrayList<EnemyBullet> enemyBulletArray;
    int enemyBulletWidth = tileSize / 8;
    int enemyBulletHeight = tileSize / 2;

    // Timer
    Timer gameLoop;

    // Movement flags
    boolean moveLeft = false;
    boolean moveRight = false;

    //STAGE DOMAIN
    private StageManager stageManager;
    private boolean gameLoopRunning = false;

    
    //MAIN EQUINOX GAME CONSTRUCTOR
    //ALL THE LOGIC HERE IS CONTAINED
    EquinoxGameLogic() {
        //SetFrame
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.DARK_GRAY);

        setFocusable(true);
        addKeyListener(this);
        //GameState init
        gameState = new GameState();
        gameState.currentStage = new Stage(1,7);


        


        // Image loading
        // Creatures
        shipImg = new ImageIcon(getClass().getResource("./img/protagtest.png")).getImage();
        enemyImgVar1 = new ImageIcon(getClass().getResource("./img/enemyvar1.gif")).getImage();
        enemyImgVar2 = new ImageIcon(getClass().getResource("./img/monstertestvar2.png")).getImage();
        enemyImgVar3 = new ImageIcon(getClass().getResource("./img/monstertestvar3.png")).getImage();


        enemyImgArray = new ArrayList<Image>();
        enemyImgArray.add(enemyImgVar1);
        enemyImgArray.add(enemyImgVar2);
        enemyImgArray.add(enemyImgVar3);


        minibossImgvar1 = new ImageIcon(getClass().getResource("./img/minibossvar1.png")).getImage();
        mainbossImgvar1 = new ImageIcon(getClass().getResource("./img/mainbossvar1.png")).getImage();
        specialEnemyImgArray = new ArrayList<Image>();
        specialEnemyImgArray.add(minibossImgvar1);
        specialEnemyImgArray.add(mainbossImgvar1);
        

        // //Maps
        world1BG = new ImageIcon(getClass().getResource("./img/world1BG.png")).getImage();
        // Misc
        laserBlue = new ImageIcon(getClass().getResource("./img/laserBlue.png")).getImage();
        enemyBulletImg = new ImageIcon(getClass().getResource("./img/laserRed.png")).getImage();
        // Ship
        ship = new ShipUser(shipX, shipY, shipWidth, shipHeight, shipImg);

        // Enemy Array Group
        enemyArray = new ArrayList<Enemy>();
        enemyBulletArray = new ArrayList<EnemyBullet>();

        // Bullets
        bulletArray = new ArrayList<Bullet>();
        // Ship skills
        tacticalArray = new ArrayList<TacticalQ>();
        tacticalEArray = new ArrayList<TacticalE>();

        // Game timer
        gameLoop = new Timer(1000 / 60, this);
        createEnemies();
        gameLoop.start();
    }
    
    //Stage Manager
    public void setStageManager(StageManager stageManager) {
        this.stageManager = stageManager;
    }
    //GAMESTATE GETTER FOR CUTSCENES
    public GameState getGameState() {
        return gameState;
    }

    // Draw assets
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (world1BG != null) {
            g.drawImage(world1BG, 0, 0, boardWidth, boardHeight, this);
        }
        draw(g);
    }

    // DRAW METHOD
    public void draw(Graphics g) {
        drawShip(g);
        drawEnemies(g);
        drawPlayerBullets(g);
        drawTacticalAbilities(g);
        drawEnemyBullets(g);
        drawGameStats(g);
    }
    //DRAW PLAYER SHIP FOR DRAW()
    private void drawShip(Graphics g) {
        g.drawImage(ship.img, ship.getX(), ship.getY(), ship.getWidth(), ship.getHeight(), null);
    }
    //DRAW ENEMY ARRAY FOR DRAW()
    private void drawEnemies(Graphics g) {
        for (Enemy enemy : enemyArray) {
            if (enemy.isAlive()) {
                if (enemy instanceof Miniboss) {
                    g.drawImage(enemy.img, enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight(), null);
                    drawBossHealthBar(g, (Miniboss) enemy);
                } else if (enemy instanceof MainBoss) {
                    g.drawImage(enemy.img, enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight(), null);
                    drawBossHealthBar(g, (MainBoss) enemy);
                } else {
                    g.drawImage(enemy.img, enemy.getX(), enemy.getY(), enemy.getWidth(), enemy.getHeight(), null);
                }
            }
        }
    }
    //DRAW PLAYER BULLET FOR DRAW()
    private void drawPlayerBullets(Graphics g) {
        g.setColor(Color.white);
        for (Bullet bullet : bulletArray) {
            if (!bullet.isUsed()) {
                g.drawImage(laserBlue, bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight(), null);
            }
        }
    }
    //DRAW TACTICALS FOR DRAW()
    private void drawTacticalAbilities(Graphics g) {
        //TACTICAL Q
        g.setColor(Color.white);
        for (TacticalQ tacticalq : tacticalArray) {
            if (!tacticalq.isUsed()) {
                g.fillRect(tacticalq.getX(), tacticalq.getY(), tacticalq.getWidth(), tacticalq.getHeight());
            }
        }
        //TACTICAL E
        for (TacticalE tacticale : tacticalEArray) {
            if (!tacticale.isUsed()) {
                g.fillRect(tacticale.getX(), tacticale.getY(), tacticale.getWidth(), tacticale.getHeight());
            }
        }
    }
    //DRAW ENEMY BULLETS FOR DRAW()
    private void drawEnemyBullets(Graphics g) {
        for (EnemyBullet bullet : enemyBulletArray) {
            g.drawImage(enemyBulletImg, bullet.getX(), bullet.getY(), bullet.getWidth(), bullet.getHeight(), null);
        }
    }
    //DRAW GAME STATS FOR DRAW()
    private void drawGameStats(Graphics g) {
        // Draw Score
        g.setColor(Color.LIGHT_GRAY);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        if (gameState.gameOver) {
            g.drawString("Game Over: " + String.valueOf(gameState.score), 10, 35);
        } else {
            g.drawString("Score: " + String.valueOf(gameState.score), 10, 35);
        }
        // Draw Money
        g.setColor(Color.ORANGE);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Money: " + gameState.money, 10, 60);
        // Draw Killed
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.drawString("Killed: " + gameState.enemySlain, 672, 60);
        // Draw TacticalQ cooldown
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (remainingCooldown > 0) {
            g.drawString("Tactical Q: " + String.format("%.1f", (double) remainingCooldown / 1000) + "s", 10,
                    tileSize * 24);
        } else {
            g.drawString("Tactical Q: Ready", 10, tileSize * 24);
        }
        // Draw TacticalE cooldown
        if (remainingCooldownE > 0) {
            g.drawString("Tactical E: " + String.format("%.1f", (double) remainingCooldownE / 1000) + "s", 10 + tileSize * 5,
                    tileSize * 24);
        } else {
            g.drawString("Tactical E: Ready", 10 + tileSize * 5, tileSize * 24);
        }
        // Draw Stage and Wave String
        g.drawString("World: " + gameState.currentStage.getStageNumber() + " Wave: " + gameState.currentStage.getCurrentWave(), 10, 85);
    }
    //DRAW BOSS HEALTH BARS FOR DRAW()
    private void drawBossHealthBar(Graphics g, Miniboss miniboss) {
        if (miniboss.isAlive()) {
            int healthBarWidth = boardWidth / 2; // Half the width of the board
            int healthBarHeight = 20;
            int healthBarX = boardWidth / 4; // Center the health bar
            int healthBarY = 40; // Position it below the top
            //NAME
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("Miniboss: "+miniboss.getEnemyBossName(), boardWidth/3, 60);
            // Calculate the filled portion of the health bar
            double healthPercentage = (double) miniboss.getHitpoints() / miniboss.getMaxHitpoints();
            int filledWidth = (int) (healthBarWidth * healthPercentage);

            // Draw the background of the health bar
            g.setColor(Color.GRAY);
            g.fillRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);

            // Draw the filled portion of the health bar
            g.setColor(Color.RED);
            g.fillRect(healthBarX, healthBarY, filledWidth, healthBarHeight);

            // Draw the border of the health bar
            g.setColor(Color.BLACK);
            g.drawRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
        }
    }
    private void drawBossHealthBar(Graphics g, MainBoss mainboss) {
        if (mainboss.isAlive()) {
            int healthBarWidth = boardWidth / 2; // Half the width of the board
            int healthBarHeight = 20;
            int healthBarX = boardWidth / 4; // Center the health bar
            int healthBarY = 40; // Position it below the top
            //NAME
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("BOSS: "+mainboss.getEnemyBossName(), boardWidth/3, 60);
            // Calculate the filled portion of the health bar
            double healthPercentage = (double) mainboss.getHitpoints() / mainboss.getMaxHitpoints();
            int filledWidth = (int) (healthBarWidth * healthPercentage);

            // Draw the background of the health bar
            g.setColor(Color.GRAY);
            g.fillRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);

            // Draw the filled portion of the health bar
            g.setColor(Color.RED);
            g.fillRect(healthBarX, healthBarY, filledWidth, healthBarHeight);

            // Draw the border of the health bar
            g.setColor(Color.BLACK);
            g.drawRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
        }
    }
    // MAIN MOVE GAME FUNCTION
    public void moveGame() {
        if (gameLoopRunning) {
            moveEnemies();
            moveEnemyBullets();
            movePlayerBullets();
            moveTacticalAbilities();
            checkBulletCollisions();
            checkTacticalCollisions();
            checkEnemyBulletCollisions();
            clearOffScreenBullets();
            handleStageAndWaveLogic();
            updateCooldowns();
            moveShip();
        }
    }
    
    //GAME LOOP HANDLING
    public void startGameLoop(){
        gameLoopRunning = true;
        gameLoop.start();
    }
    public void stopGameLoop(){
        gameLoopRunning = false;
        gameLoop.stop();
    }
    
    // MOVE ENEMIES for MOVE GAME FUNCTION
    private void moveEnemies() {
        for (Enemy enemy : enemyArray) {
            if (enemy.isAlive()) {
                int enemyWidthToUse = enemy.getWidth();
                if(enemy instanceof Miniboss){
                    enemyWidthToUse = enemy.getWidth();
                }else if(enemy instanceof MainBoss){
                    enemyWidthToUse = enemy.getWidth();
                }else{
                    enemyWidthToUse = enemyWidth;
                }
                enemy.move(boardWidth, enemyWidthToUse, enemyHeight);
                if (enemy.isMoveDown()) {
                    enemy.moveDown(enemyHeight);
                }
                if (enemy instanceof ShootingEnemy) {
                    ((ShootingEnemy) enemy).shoot(enemyBulletArray, enemyBulletImg, enemyBulletWidth, enemyBulletHeight);
                }
                if (enemy instanceof Miniboss) {
                    // Only call moveY if not moving down
                    if (!enemy.isMoveDown()) {
                        ((Miniboss) enemy).moveY();
                    }
                    ((Miniboss) enemy).shoot(enemyBulletArray, enemyBulletImg, enemyBulletWidth, enemyBulletHeight);
                }
                if (enemy instanceof MainBoss) {
                    // Only call moveY if not moving down
                    if (!enemy.isMoveDown()) {
                        ((MainBoss) enemy).moveY();
                    }
                    ((MainBoss) enemy).shoot(enemyBulletArray, enemyBulletImg, enemyBulletWidth, enemyBulletHeight);
                }
                // Lose condition
                if (enemy.getY() >= ship.getY()) {
                    gameState.gameOver = true;
                }
            }
        }
    }
    //MOVE ENEMY BULLETS for MOVE GAME FUNCTION
    private void moveEnemyBullets() {
        for (EnemyBullet bullet : enemyBulletArray) {
            bullet.move();
        }
    }
    //MOVE PLAYER BULLETS for MOVE GAME FUNCTION
    private void movePlayerBullets() {
        for (Bullet bullet : bulletArray) {
            bullet.setY(bullet.getY() + bulletVelocityY);
        }
    }
    // MOVE SKILLS for MOVE GAME FUNCTION
    private void moveTacticalAbilities() {
        for (TacticalQ tacticalq : tacticalArray) {
            tacticalq.setY(tacticalq.getY() + tacticalqVelocityY);
        }
        for (TacticalE tacticale : tacticalEArray) {
            tacticale.setY(tacticale.getY() + tacticaleVelocityY);
        }
    }
    //CHECK BULLET COLLISIONS for MOVE GAME FUNCTION
    private void checkBulletCollisions() {
        for (Bullet bullet : bulletArray) {
            if (!bullet.isUsed()) {
                for (Enemy enemy : enemyArray) {
                    if (enemy.isAlive() && detectCollision(bullet, enemy)) {
                        bullet.setUsed(true);
                        handleEnemyHit(enemy);
                    }
                }
            }
        }
    }
    //CHECK SKILL HIT for MOVE GAME FUNCTION
    private void checkTacticalCollisions() {
        for (TacticalQ tacticalq : tacticalArray) {
            if (!tacticalq.isUsed()) {
                for (Enemy enemy : enemyArray) {
                    if (enemy.isAlive() && detectCollision(tacticalq, enemy)) {
                        handleEnemyHit(enemy);
                    }
                }
            }
        }
        for (TacticalE tacticale : tacticalEArray) {
            if (!tacticale.isUsed()) {
                for (Enemy enemy : enemyArray) {
                    if (enemy.isAlive() && detectCollision(tacticale, enemy)) {
                        tacticale.setUsed(true);
                        handleEnemyHit(enemy);
                    }
                }
            }
        }
    }
    //ENEMY BULLET CHECK COLLISION for MOVE GAME FUNCTION
    private void checkEnemyBulletCollisions(){
        for (EnemyBullet bullet : enemyBulletArray) {
            // Bullet collision check
            if(detectCollision(bullet, ship)){
                gameState.gameOver = true;
            }
        }
    }
    //ENEMY HIT for MOVE GAME FUNCTION
    private void handleEnemyHit(Enemy enemy) {
        if (enemy instanceof SpecialEnemy) {
            SpecialEnemy specialEnemy = (SpecialEnemy) enemy;
            specialEnemy.setHitpoints(specialEnemy.getHitpoints() - 1);
            if (specialEnemy.getHitpoints() <= 0) {
                if(enemy instanceof Miniboss){
                    gameState.score += 10000;   //SCORES
                    gameState.money += 2500;    //MONEY
                    gameState.enemySlain++;     //STAT COUNTER
                }else if(enemy instanceof MainBoss){
                    gameState.score += 20000;   //SCORES
                    gameState.money += 5000;    //MONEY
                    gameState.enemySlain++;     //STAT COUNTER
                }
                enemy.setAlive(false);
                gameState.enemyCount--;
            }
        } else {
            enemy.setAlive(false);
            gameState.dropLoot();       //RANDOM MONEY
            gameState.score += 100;
            gameState.enemySlain++;     //STAT COUNTER
            gameState.enemyCount--;
        }
    }
    //OPTIMIZATIONS for MOVE GAME FUNCTION
    private void clearOffScreenBullets() {
        bulletArray.removeIf(bullet -> bullet.isUsed() || bullet.getY() < 0);
        tacticalArray.removeIf(tacticalq -> tacticalq.isUsed() || tacticalq.getY() < 0);
        tacticalEArray.removeIf(tacticale -> tacticale.isUsed() || tacticale.getY() < 0);
        enemyBulletArray.removeIf(enemyBullet -> enemyBullet.isUsed() || enemyBullet.getY() > boardHeight);
    }
    //STAGE WAVE LOGIC for MOVE GAME FUNCTION
    private void handleStageAndWaveLogic() {
        // Next wave of enemies
        if (gameState.enemyCount == 0) {
            stopGameLoop();
            if (gameState.currentStage.getCurrentWave() == gameState.currentStage.getTotalWaves()) {
                // Move to the next stage
                gameState.currentStage.setStageNumber(gameState.currentStage.getStageNumber() + 1);
                gameState.currentStage.setCurrentWave(1);
                gameState.currentStage.setSpecialEnemySpawned(false);
                reset();
                stageManager.startCutscene();
            } else if (gameState.currentStage.getCurrentWave() == gameState.currentStage.getTotalWaves() - 1) {
                // Spawn the mainboss
                gameState.currentStage.setSpecialEnemySpawned(true);
                createMainboss();
                gameState.currentStage.setCurrentWave(gameState.currentStage.getCurrentWave() + 1);
                startGameLoop();
            } else if (gameState.currentStage.getCurrentWave() == gameState.currentStage.getTotalWaves() - 2) {
                // Spawn the miniboss
                gameState.currentStage.setSpecialEnemySpawned(true);
                createMiniboss();
                gameState.currentStage.setCurrentWave(gameState.currentStage.getCurrentWave() + 1);
                startGameLoop();
            } else {
                // Move to the next wave
                gameState.currentStage.setCurrentWave(gameState.currentStage.getCurrentWave() + 1);
                reset();
                startGameLoop(); // Add this line to restart the game loop
            }
        }
    }
    // Cooldowns for MOVE GAME FUNCTION
    private void updateCooldowns() {
        long currentTime = System.currentTimeMillis();
        remainingCooldown = tacticalQCooldown - (currentTime - lastTacticalQUseTime);
        if (remainingCooldown < 0) {
            remainingCooldown = 0;
        }
        remainingCooldownE = tacticalECooldown - (currentTime - lastTacticalEUseTime);
        if (remainingCooldownE < 0) {
            remainingCooldownE = 0;
        }
    }
    // MOVE SHIP for MOVE GAME FUNCTION
    private void moveShip() {
        // Ship movement
        if (moveLeft) {
            shipVelocityX = Math.max(shipVelocityX - shipAcceleration, -shipMaxSpeed);
        } else if (moveRight) {
            shipVelocityX = Math.min(shipVelocityX + shipAcceleration, shipMaxSpeed);
        } else {
            // Deceleration
            if (shipVelocityX > 0) {
                shipVelocityX = Math.max(0, shipVelocityX - shipDeceleration);
            } else if (shipVelocityX < 0) {
                shipVelocityX = Math.min(0, shipVelocityX + shipDeceleration);
            }
        }

        // Move the ship
        int newShipX = ship.getX() + shipVelocityX;
        if (newShipX >= 0 && newShipX + ship.getWidth() <= boardWidth) {
            ship.setX(newShipX);
        }
    }
    //RESET for MOVE GAME FUNCTION
    public void reset(){
        enemyArray.clear();
        bulletArray.clear();
        enemyBulletArray.clear();
        tacticalArray.clear();
        tacticalEArray.clear();
        createEnemies();
    }
    //CREATING ENEMY INSTANCES
    // Create enemies
    public void createEnemies() {
        Random random = new Random();
        int currentWave = gameState.currentStage.getCurrentWave();
        int maxEnemyRows = enemyRows + (currentWave / 2); // Increase rows every 2 waves
        int maxEnemyColumns = enemyColumns + (currentWave); // Increase columns every 3 waves
        maxEnemyRows = Math.min(maxEnemyRows, 15); // Limit to max 15 rows
        maxEnemyColumns = Math.min(maxEnemyColumns, 11); // Limit to max 15 columns

        for (int r = 0; r < maxEnemyRows; r++) {
            for (int c = 0; c < maxEnemyColumns; c++) {
                int randomImgIndex = random.nextInt(enemyImgArray.size());
                int enemyType = random.nextInt(6); // Now 0-5 (6 possibilities)
                Enemy enemy;
                if (enemyType == 3) { // 16.66% Chance to spawn Shooting Enemy
                    enemy = new ShootingEnemy(
                            enemyX + c * enemyWidth,
                            enemyY + r * enemyHeight,
                            enemyWidth,
                            enemyHeight,
                            enemyImgArray.get(randomImgIndex),
                            enemyVelocityX
                    );
                } else { 
                    enemy = new FastEnemy(
                            enemyX + c * enemyWidth,
                            enemyY + r * enemyHeight,
                            enemyWidth,
                            enemyHeight,
                            enemyImgArray.get(randomImgIndex),
                            enemyVelocityX
                    );
                    
                }
                enemyArray.add(enemy);
            }
        }
        gameState.enemyCount = enemyArray.size();
    }
    //SPECIAL ENEMIES
    public void createMiniboss(){
        int minibossWorld1 = 0;
        Miniboss miniboss = new Miniboss(boardWidth/2 - tileSize * 2,
                tileSize,
                tileSize*4,
                tileSize*4,
                specialEnemyImgArray.get(minibossWorld1),
                enemyVelocityX,
                specialEnemyImgArray.get(minibossWorld1),
                100,
                100,
                2,
                "Quantum Anomaly"
                );
        enemyArray.add(miniboss);
        gameState.enemyCount++; // Increment
    }
    public void createMainboss(){
        int mainbossWorld1 = 1;
        MainBoss mainboss = new MainBoss(boardWidth/2 - tileSize * 4,
                tileSize,
                tileSize*8,
                tileSize*8,
                specialEnemyImgArray.get(mainbossWorld1),
                enemyVelocityX,
                specialEnemyImgArray.get(mainbossWorld1),
                500,
                75,
                2,
                "The Collector"
                );
        enemyArray.add(mainboss);
        gameState.enemyCount++; // Increment
    }
    //COLLISION LOGIC
    public boolean detectCollision(Block a, Block b) {
        return a.getX() < b.getX() + b.getWidth() && // entity a's top left corner doesn't reach b's top right corner
                a.getX() + a.getWidth() > b.getX() && // entity a's top right corner passes b's top left corner
                a.getY() < b.getY() + b.getHeight() && // entity a's top left corner doesn't reach b's bottom left corner
                a.getY() + a.getHeight() > b.getY(); // entity a's bottom left corner passes b's top let corner
    }

    //Interfaced from Action Listener
    @Override
    public void actionPerformed(ActionEvent e) {

        moveGame();
        repaint();
        if (gameState.gameOver) {
            gameLoop.stop();
        }
    }
    //Interfaced from Key Listener to listen for keyboard
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Ship movement flag raise
        if (e.getKeyCode() == KeyEvent.VK_A) {
            moveLeft = true;
        } else if (e.getKeyCode() == KeyEvent.VK_D) {
            moveRight = true;
        }
    }
    //KEY TAP LOGIC
    @Override
    public void keyReleased(KeyEvent e) {
        //KEY PRESS LOGIC
        // Ship movement flag released
        if (e.getKeyCode() == KeyEvent.VK_A) {
            moveLeft = false;
        } else if (e.getKeyCode() == KeyEvent.VK_D) {
            moveRight = false;
        } 
        //Ship Basic Attack Trigger
        else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            Bullet bullet = new Bullet(ship.getX() + ship.getWidth() * 15 / 32, ship.getY(), bulletWidth, bulletHeight,
                    laserBlue);
            bulletArray.add(bullet);
        } 
        // TacticalQ Trigger
        else if (e.getKeyCode() == KeyEvent.VK_Q) {
            
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastTacticalQUseTime >= tacticalQCooldown) {
                // Cooldown is over, allow the ability
                TacticalQ tacticalq = new TacticalQ(ship.getX() + ship.getWidth() * 15 / 32, ship.getY(),
                        tacticalqWidth, tacticalqHeight, null);
                tacticalArray.add(tacticalq);
                lastTacticalQUseTime = currentTime; // Update the last use time
            } else {
                // Ability is on cooldown
                System.out.println("Tactical Q on cooldown!");
            }
        } 
        // TacticalE Trigger
        else if (e.getKeyCode() == KeyEvent.VK_E) {
            
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastTacticalEUseTime >= tacticalECooldown) {
                // Cooldown is over, allow the ability
                int numBullets = boardWidth / tileSize; // Number of bullets based on board width
                int startX = 0; // Start at the left edge of the screen
                for (int i = 0; i < numBullets; i++) {
                    TacticalE tacticale = new TacticalE(startX + i * tileSize, ship.getY(),
                            tacticaleWidth, tacticaleHeight, null);
                    tacticalEArray.add(tacticale);
                }
                lastTacticalEUseTime = currentTime; // Update the last use time
            } 
        // SPECIAL Trigger
        
        
            else {
                // Ability is on cooldown
                System.out.println("Tactical E on cooldown!");
            }
        }

    }
}
