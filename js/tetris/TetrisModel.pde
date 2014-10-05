/**
    * Holds the 'logic' and 'data' of the game
    * This class is intended to not have to worry about
    * user events (TetrisController) or drawing (TetrisDrawer)
    *
    * Created by RAPHAEL on 9/22/2014.
    */
public class TetrisModel {
    private long lastTimeMillis = 0;

    // colors
    public int activeEyeBlockColor = color(0, 0, 0, 230); // 0xff000000; //0xff818CC7; // light blue
    public int rightEyeBlockColor = color(0, 0, 0, 16); // 0x11000000; //0xff182461; // dark blue
    public int bgColor = color(255, 255, 255, 255); // color(16, 27, 82, 20); // 0xff101B52; // darker blue

    //int borderX = 200;
    public int[] row = {0, 0, 1, 1};
    public int[] col = {5, 4, 5, 4};
    public int levelwidth = 10;
    public int levelheight = 20;
    public int[] blockArray = new int[levelwidth * levelheight];
    public int time = 0;
    public int score = 0;
    public int updateSpeed = 20;
    public int level = 1;
    public int linesCleared = 0;

    private int currentBlockType = Block.SQUARE;
    private int currentBlockRotation = 0;//0, 90, 180, 270
    //this is how many degrees
    //clockwise it is
    public int currentBlockColor = color(0, random(0, 255), random(0, 255)); // (int)(Math.random() * 255 + 255 * 255) + 1;
    public int nextBlockColor = color(0, random(0, 255), random(0, 255)); // (int)(Math.random() * 255 + 255 * 255) + 1;
    public int nextBlockType = Block.getRandomBlock();

    /** Game state **/
    public int gameState = GameState.IN_GAME;

    /**  key time constants
     * Ensures that buttons aren't clicked too quickly on accident
     **/
    private int defaultLagTime = 200;
    private int lagChange = 150; // used for holding a button
    private int leftLag  = defaultLagTime; // aka at least .2 seconds between lefts
    private int rightLag = defaultLagTime;
    private int upLag    = defaultLagTime * 2;
    private long lastLeftPressTime = 0;
    private long lastRightPressTime = 0;
    private long lastUpPressTime = 0;
    private long lastPausePressTime = 0;

    /**
     * Resets all variables so that the
     * game can start anew
     */
    public void reset() {
        row = new int[]{0, 0, 1, 1};
        col = new int[]{5, 4, 5, 4};
        blockArray = new int[levelwidth * levelheight];
        time = 0;
        score = 0;
        updateSpeed = 20;
        level = 1;
        linesCleared = 0;
        currentBlockType = Block.SQUARE;
        currentBlockRotation = 0;
        currentBlockColor = color(0, random(0, 255), random(0, 255)); // (int)(Math.random() * 255 + 255 * 255) + 1;
        nextBlockColor = color(0, random(0, 255), random(0, 255)); // 0x000000 + (int)(Math.random() * 255 + 255 * 255) + 1;
        nextBlockType = Block.getRandomBlock();

        gameState = GameState.IN_GAME;
        // mainMenu = false;
        // gameOver = false;
        // paused = false;
        leftLag = defaultLagTime;
        rightLag = defaultLagTime;
        upLag = defaultLagTime *2;
        lastLeftPressTime = 0;
        lastRightPressTime = 0;
        lastUpPressTime = 0;
        lastPausePressTime = 0;
    }

    /** blockArray getter and setter **/
    public int getBlock(int column, int row)             { return blockArray[column + row * levelwidth];  }
    public void setBlock(int column, int row, int value) { blockArray[column + row * levelwidth] = value; }

    /**
     * Checks whether the current shape will collide with something if it goes down one row
     */
    public boolean bottomCollision() {
        for(int i = 0; i < 4; i++) {

            int btmCollisionCol = col[i];
            int btmCollisionRow = row[i] + 1;

            // if at the bottom, there's a bottom collision
            if(btmCollisionRow >= levelheight) {
                return true;
            }

            int btmCollisionColor= blockArray[btmCollisionCol +
                    btmCollisionRow * levelwidth];

            // if the space below isn't empty
            // and you didn't collide with yourself
            // return true
            if(btmCollisionColor != 0) {
                boolean selfcollision = false;
                for(int j = 0; j < 4; j++) {
                    if(col[j] == btmCollisionCol &&
                            row[j] == btmCollisionRow) {
                        selfcollision = true;
                        break;
                    }
                }
                if(selfcollision == false) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the current shape will collide with something if it goes left one row
     */
    private boolean leftCollision() {
        for(int i = 0; i < 4; i++) {
            // if touching left, there's a left collision
            if(col[i] == 0) {
                return true;
            }
            // check blocks on your left
            if(getBlock(col[i] - 1, row[i]) != 0) {
                boolean selfcollision = false;
                for(int j = 0; j < 4; j++) {
                    if(col[i] - 1 == col[j] &&
                            row[i] == row[j]) {
                        selfcollision = true;
                    }
                }
                if(selfcollision == false) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the current shape will collide with something if it goes right one row
     */
    private boolean rightCollision() {
        for(int i = 0; i < 4; i++) {
            // if touching right, there's a right collision
            if(col[i] == 9) {
                return true;
            }
            // check blocks on your right
            if(getBlock(col[i] + 1, row[i]) != 0) {
                boolean selfcollision = false;
                for(int j = 0; j < 4; j++) {
                    if(col[i] + 1 == col[j] &&
                            row[i] == row[j]) {
                        selfcollision = true;
                    }
                }
                if(selfcollision == false) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the current shape is currently colliding with something
     * Used to test for game over
     */
    private boolean collision() {
        for(int i = 0; i < 4; i++) {
            if(col[i] < 0 || col[i] >= levelwidth ||
                    row[i] < 0 || row[i] >= levelheight) {
                return true;
            }
            if(getBlock(col[i], row[i]) != 0) {
                return true;
            }
        }

        return false;
    }

    /**
     * Method to rotate the current block
     */
    private void rotateBlock() {

        int oldBlockRotation = currentBlockRotation;

        currentBlockRotation += 90;
        currentBlockRotation %= 360;

        int[] oldrow = {0, 0, 0, 0};
        int[] oldcol = {0, 0, 0, 0};
        int[] newrow = {0, 0, 0, 0};
        int[] newcol = {0, 0, 0, 0};

        for(int i = 0; i < 4; i++) {
            oldrow[i] = row[i];
            oldcol[i] = col[i];
        }

        if(currentBlockType == Block.SQUARE) { // square
            newrow = row;
            newcol = col;
        }
        else if(currentBlockType == Block.REGULAR_L) { // normal l
            if(currentBlockRotation == 0) {
                // commented these arrays to help me understand
                // how far each block should move
                //row = [0, 1, 2, 2]
                //col = [4, 4, 4, 5]
                newrow = new int[]{row[0] - 1, row[1],
                        row[2] + 1, row[3] + 2};
                newcol = new int[]{col[0] + 1, col[1],
                        col[2] - 1, col[3]};
            }
            else if(currentBlockRotation == 90) {
                //row = [1, 1, 1, 2]
                //col = [5, 4, 3, 3]
                newrow = new int[]{row[0] + 1, row[1],
                        row[2] - 1, row[3]};
                newcol = new int[]{col[0] + 1, col[1],
                        col[2] - 1, col[3] - 2};
            }
            else if(currentBlockRotation == 180) {
                //row = [2, 1, 0, 0]
                //col = [4, 4, 4, 3]
                newrow = new int[]{row[0] + 1, row[1],
                        row[2] - 1, row[3] - 2};
                newcol = new int[]{col[0] - 1, col[1],
                        col[2] + 1, col[3]};
            }
            else if(currentBlockRotation == 270) {
                //row = [1, 1, 1, 0]
                //col = [3, 4, 5, 5]
                newrow = new int[]{row[0] - 1, row[1],
                        row[2] + 1, row[3]};
                newcol = new int[]{col[0] - 1, col[1],
                        col[2] + 1, col[3] + 2};
            }

        }
        else if(currentBlockType == Block.BACKWARDS_L) { // backwards l
            if(currentBlockRotation == 0) {
                //row = [0, 1, 2, 2};
                //col = [5, 5, 5, 4};
                newrow = new int[]{row[0] - 1, row[1],
                        row[2] + 1, row[3]};
                newcol = new int[]{col[0] + 1, col[1],
                        col[2] - 1, col[3] - 2};
            }
            else if(currentBlockRotation == 90) {
                //row = [1, 1, 1, 0};
                //col = [6, 5, 4, 4};
                newrow = new int[]{row[0] + 1, row[1],
                        row[2] - 1, row[3] - 2};
                newcol = new int[]{col[0] + 1, col[1],
                        col[2] - 1, col[3]};
            }
            else if(currentBlockRotation == 180) {
                //row = [2, 1, 0, 0};
                //col = [5, 5, 5, 6};
                newrow = new int[]{row[0] + 1, row[1],
                        row[2] - 1, row[3]};
                newcol = new int[]{col[0] - 1, col[1],
                        col[2] + 1, col[3] + 2};
            }
            else if(currentBlockRotation == 270) {
                //row = [1, 1, 1, 2};
                //col = [4, 5, 6, 6};
                newrow = new int[]{row[0] - 1, row[1],
                        row[2] + 1, row[3] + 2};
                newcol = new int[]{col[0] - 1, col[1],
                        col[2] + 1, col[3]};
            }
        }
        else if(currentBlockType == Block.ZIGZAG_HIGH_LEFT) { // zigzag
            if(currentBlockRotation == 0) {
                //row = [1, 1, 0, 0};
                //col = [4, 5, 5, 6};
                newrow = new int[]{row[0] - 1, row[1],
                        row[2] - 1, row[3]};
                newcol = new int[]{col[0] - 1, col[1],
                        col[2] + 1, col[3] + 2};
            }
            else if(currentBlockRotation == 90) {
                //row = [0, 1, 1, 2};
                //col = [5, 5, 6, 6};
                newrow = new int[]{row[0] - 1, row[1],
                        row[2] + 1, row[3] + 2};
                newcol = new int[]{col[0] + 1, col[1],
                        col[2] + 1, col[3]};
            }
            else if(currentBlockRotation == 180) {
                //row = [1, 1, 2, 2};
                //col = [6, 5, 5, 4};
                newrow = new int[]{row[0] + 1, row[1],
                        row[2] + 1, row[3]};
                newcol = new int[]{col[0] + 1, col[1],
                        col[2] - 1, col[3] - 2};
            }
            else if(currentBlockRotation == 270) {
                //row = [2, 1, 1, 0};
                //col = [5, 5, 4, 4};
                newrow = new int[]{row[0] + 1, row[1],
                        row[2] - 1, row[3] - 2};
                newcol = new int[]{col[0] - 1, col[1],
                        col[2] - 1, col[3]};
            }
        }
        else if(currentBlockType == Block.ZIGZAG_HIGH_RIGHT) { // backwards zigzag (low left/high right)
            if(currentBlockRotation == 0) {
                //row = [1, 1, 2, 2};
                //col = [4, 5, 5, 6};
                newrow = new int[]{row[0] - 1, row[1],
                        row[2] + 1, row[3] + 2};
                newcol = new int[]{col[0] - 1, col[1],
                        col[2] - 1, col[3]};
            }
            else if(currentBlockRotation == 90) {
                //row = [0, 1, 1, 2};
                //col = [5, 5, 4, 4};
                newrow = new int[]{row[0] - 1, row[1],
                        row[2] - 1, row[3]};
                newcol = new int[]{col[0] + 1, col[1],
                        col[2] - 1, col[3] - 2};
            }
            else if(currentBlockRotation == 180) {
                //row = [1, 1, 0, 0};
                //col = [6, 5, 5, 4};
                newrow = new int[]{row[0] + 1, row[1],
                        row[2] - 1, row[3] - 2};
                newcol = new int[]{col[0] + 1, col[1],
                        col[2] + 1, col[3]};
            }
            else if(currentBlockRotation == 270) {
                //row = [2, 1, 1, 0};
                //col = [5, 5, 6, 6};
                newrow = new int[]{row[0] + 1, row[1],
                        row[2] + 1, row[3]};
                newcol = new int[]{col[0] - 1, col[1],
                        col[2] + 1, col[3] + 2};
            }
        }
        else if(currentBlockType == Block.LINE) { // line
            if(currentBlockRotation == 0) {
                //row = [0, 1, 2, 3};
                //col = [5, 5, 5, 5};
                newrow = new int[]{row[0] - 1, row[1],
                        row[2] + 1, row[3] + 2};
                newcol = new int[]{col[0] - 2, col[1] - 1,
                        col[2], col[3] + 1};
            }
            else if(currentBlockRotation == 90) {
                //row = [1, 1, 1, 1};
                //col = [7, 6, 5, 4};
                newrow = new int[]{row[0] + 1, row[1],
                        row[2] - 1, row[3] - 2};
                newcol = new int[]{col[0] + 2, col[1] + 1,
                        col[2], col[3] - 1};
            }
            else if(currentBlockRotation == 180) {
                //row = [0, 1, 2, 3};
                //col = [6, 6, 6, 6};
                newrow = new int[]{row[0] - 1, row[1],
                        row[2] + 1, row[3] + 2};
                newcol = new int[]{col[0] - 1, col[1],
                        col[2] + 1, col[3] + 2};
            }
            else if(currentBlockRotation == 270) {
                //row = [1, 1, 1, 1};
                //col = [7, 6, 5, 4};
                newrow = new int[]{row[0] + 1, row[1],
                        row[2] - 1, row[3] - 2};
                newcol = new int[]{col[0] + 1, col[1],
                        col[2] - 1, col[3] - 2};
            }
        }
        else if(currentBlockType == Block.T) { // T
            if(currentBlockRotation == 0) {
                //row = [0, 1, 1, 1};
                //col = [5, 4, 5, 6};
                newrow = new int[]{row[0] - 1, row[1] - 1,
                        row[2], row[3] + 1};
                newcol = new int[]{col[0] + 1, col[1] - 1,
                        col[2], col[3] + 1};
            }
            else if(currentBlockRotation == 90) {
                //row = [1, 0, 1, 2};
                //col = [6, 5, 5, 5};
                newrow = new int[]{row[0] + 1, row[1] - 1,
                        row[2], row[3] + 1};
                newcol = new int[]{col[0] + 1, col[1] + 1,
                        col[2], col[3] - 1};
            }
            else if(currentBlockRotation == 180) {
                //row = [2, 1, 1, 1};
                //col = [5, 6, 5, 4};
                newrow = new int[]{row[0] + 1, row[1] + 1,
                        row[2], row[3] - 1};
                newcol = new int[]{col[0] - 1, col[1] + 1,
                        col[2], col[3] - 1};
            }
            else if(currentBlockRotation == 270) {
                //row = [1, 2, 1, 0};
                //col = [4, 5, 5, 5};
                newrow = new int[]{row[0] - 1, row[1] + 1,
                        row[2], row[3] - 1};
                newcol = new int[]{col[0] - 1, col[1] - 1,
                        col[2], col[3] + 1};
            }
        }
        row = newrow;
        col = newcol;
        if(collision()) {
            row = oldrow;
            col = oldcol;
            currentBlockRotation = oldBlockRotation;
        }
    }

    /**
     * update block based on keypresses
     */
    void keyPressed(int k) {
        for(int i = 0; i < 4; i++)
            setBlock(col[i], row[i], 0);

        if (k == Key.LEFT &&  !leftCollision() && millis() - lastLeftPressTime > leftLag){ // Left clicked, won't collide, and enough time passed
            lastLeftPressTime = millis();
            leftLag -= lagChange; // allows user to hold left to slide left
            rightLag = defaultLagTime;
            upLag = defaultLagTime;

            for(int i = 0; i < 4; i++)
                col[i]--;
        }
        if (k == Key.RIGHT && !rightCollision() && millis() - lastRightPressTime > rightLag) {
            rightLag -= lagChange; // allows user to hold right to slide right
            leftLag = defaultLagTime;
            upLag = defaultLagTime;
            lastRightPressTime = millis();

            for(int i = 0; i < 4; i++)
                col[i]++;
        }
        if (k == Key.UP && millis() - lastUpPressTime > upLag) {
            upLag -= lagChange;
            rightLag = defaultLagTime;
            leftLag = defaultLagTime;
            lastUpPressTime = millis();

            if(upLag < 100)
                upLag = 100;

            rotateBlock();
        }
        if (k == Key.DOWN && !bottomCollision()) {
            rightLag = defaultLagTime;
            leftLag = defaultLagTime;
            upLag = defaultLagTime;

            for(int i = 0; i < 4; i++)
                row[i]++;
        }

        for(int i = 0; i < 4; i++)
            setBlock(col[i], row[i], currentBlockColor);
    }

    /**
     * update block based on -accelerometer- mouse data
     */
    public void motionSensorMove() {
        int leftCol = levelwidth;
        int rightCol = -1;
        for(int i = 0; i < col.length; i++) {
            if(col[i] < leftCol) leftCol = col[i];
            if(col[i] > rightCol) rightCol = col[i];
        }
        int blockwidth = rightCol - leftCol + 1;

        int currentCol = leftCol;
        int expectedCol = (int) map(mouse_x, width/4.0, width * 3.0/4, 0, levelwidth - 1);

        if(expectedCol < currentCol) {
            keyPressed(Key.LEFT);
        } else if(expectedCol > currentCol) {
            keyPressed(Key.RIGHT);
        }
    }

    /** sets your current block to what was shown and makes a
     * random next block
     */
    private void nextBlock() {
        // swap colors, might help both eyes train
        int temp                 = rightEyeBlockColor;
        rightEyeBlockColor = activeEyeBlockColor;
        activeEyeBlockColor  = temp;

        if(nextBlockType == Block.SQUARE) { // square
            row = new int[]{0, 0, 1, 1};
            col = new int[]{5, 4, 5, 4};
        }
        else if(nextBlockType == Block.REGULAR_L) { // normal l
            row = new int[]{0, 1, 2, 2};
            col = new int[]{4, 4, 4, 5};
        }
        else if(nextBlockType == Block.BACKWARDS_L) { // backwards l
            row = new int[]{0, 1, 2, 2};
            col = new int[]{5, 5, 5, 4};
        }
        else if(nextBlockType == Block.ZIGZAG_HIGH_LEFT) { // zigzag
            row = new int[]{1, 1, 0, 0};
            col = new int[]{4, 5, 5, 6};
        }
        else if(nextBlockType == Block.ZIGZAG_HIGH_RIGHT) { // backwards zigzag
            row = new int[]{0, 0, 1, 1};
            col = new int[]{4, 5, 5, 6};
        }
        else if(nextBlockType == Block.LINE) { // line
            row = new int[]{0, 1, 2, 3};
            col = new int[]{5, 5, 5, 5};
        }
        else if(nextBlockType == Block.T) { // T
            row = new int[]{0, 1, 1, 1};
            col = new int[]{5, 4, 5, 6};
        }

        if(collision()) {
            // Log.d(MainActivity.TAG, "game over");
            gameState = GameState.GAME_OVER; // gameOver = true;
        }

        currentBlockColor = nextBlockColor;
        currentBlockType = nextBlockType;
        currentBlockRotation = 0;
        nextBlockColor = (int) Math.floor(Math.random() * 255 + 255 * 255) + 1;
        nextBlockType = Block.getRandomBlock();
    }

    /** move block down and check block under
     * if block underneath and no recent keypresses, goes to next block
     */
    public void moveDown() {
        if (!bottomCollision()) { // move down
            for(int i = 0; i < 4; i++) {
                setBlock(col[i], row[i], 0);
                row[i]++;
            }
        }
        else { // go to next block if nothing recently done
            boolean recentPress = millis() - lastLeftPressTime < defaultLagTime * 3 ||
                    millis() - lastRightPressTime < defaultLagTime * 3 ||
                    millis() - lastUpPressTime < defaultLagTime * 3;
            if(!recentPress) {
                score += 15 + linesCleared;
                nextBlock();
            }
        }

        for(int i = 0; i < 4; i++)
            setBlock(col[i], row[i], currentBlockColor);
    }

    /**
     * Makes the game go faster after you clear a line
     */
    private void setDifficulty() {
        updateSpeed = 21 - linesCleared;
        if(linesCleared >= 18) {
            updateSpeed = 3;
        }
    }

    /**
     * Removes lines
     * If lines were removed, updates score and difficulty and
     * goes to the next block
     */
    public void removeLines() {
        //from bottom to top, check if rows are filled
        boolean posReset = false;
        int row0 = levelheight - 1;
        while(row0 >= 0) {
            boolean filled = true;
            for(int col0 = 0; col0 < levelwidth; col0++) {
                if(getBlock(col0, row0) == 0) {
                    filled = false;
                    break;
                }
            }
            // if a row is filled move everything above it down
            // else check the next row up
            if(filled == true) {
                linesCleared++;

                // move the lines down
                for(int roow = row0; roow >= 1; roow--) {
                    for(int cool = 0; cool < levelwidth; cool++) {
                        setBlock(cool, roow,
                                getBlock(cool, roow - 1));
                    }
                }
                for(int i = 0; i < levelwidth; i++) {
                    blockArray[i] = 0;
                }

                score += 100;

                // only reset your block once
                if(posReset == false) {
                    nextBlock();
                    posReset = true;
                }

                setDifficulty();
            }
            else {
                row0--;
            }
        }
    }

    /**
     * Called when a key is released
     * Not added yet
     * This is where the pause button pausing would be called
     */
    private void keyReleased() {
        //if(keyCode == 80) {
        //    paused = !paused;
        //}
    }

    /**
     * Called when the "action button" is pressed
     * Action button = Cardboard magnet, screen touch, or tilting phone up
     *
     * Rotates block in-game
     */
    public void actionButton() {
        if(gameState == GameState.IN_GAME) {
            // Always give user feedback
            keyPressed(Key.UP);
        } else if(gameState == GameState.GAME_OVER) {
            reset();
        } else if(gameState == GameState.PAUSED) {
            // un pause
        } else if(gameState == GameState.MAIN_MENU) {
            // perform selected action

            gameState = GameState.IN_GAME;
        } else if(gameState == GameState.MAIN_MENU_SETTINGS) {
            // perform selected action - turn off music or calibrate accelerometer
        }
    }

    /**
     * Constructor
     * Instantiates variables
     */
    public TetrisModel() {
        for(int r = 0; r < levelwidth; r++) {
            for(int c = 0; c < levelheight; c++) {
                blockArray[r + c * levelwidth] = 0;
            }
        }
    }
}

