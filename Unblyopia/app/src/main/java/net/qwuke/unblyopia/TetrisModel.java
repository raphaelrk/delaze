package net.qwuke.unblyopia;

import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Vibrator;
import android.util.Log;

/**
    * Holds the 'logic' and 'data' of the game
    * This class is intended to not have to worry about
    * user events (TetrisController) or drawing (TetrisDrawer)
    *
    * Created by RAPHAEL on 9/22/2014.
    */
public class TetrisModel {
    private long lastTimeMillis = 0;

    private Canvas currCanvas = null;
    private final MotionSensorModule motionSensor;
    private final Vibrator vibrator;

    // colors
    private float initYaw;
    private float initPitch;
    private static int activeEyeBlockColor; //0xff182461; // dark blue
    public static int bgColor; //0xff101B52; // darker blue
    public static int fallenColour; //= 0xff818CCD;

    // used in keyPress method
    public enum Input {
        LEFT, RIGHT, UP, DOWN
    }

    private static final int LR_VIBRATE_DURATION = 10; // duration of vibration in ms for left/right movement

    //int borderX = 200;
    public int[] row = {0, 0, 1, 1};
    public int[] col = {5, 4, 5, 4};
    private int[] colours = new int[4];
    public static final int levelwidth = 10;
    public static final int levelheight = 20;
    private int[] blockArray = new int[levelwidth * levelheight];
    public int time = 0;
    public int score = 0;
    private int inputSpeed = 0;
    private int baseSpeed = 20;
    public int updateSpeed = 20;
    public int linesCleared = 0;

    public enum Block {
        /** Former Block constants **
         private int singleBlock = 0;
         private int squareBlock = 1;
         private int regularLBlock = 2;
         private int backwardsLBlock = 3;
         private int zigzagHighLeftBlock = 4;
         private int zigzagLowLeftBlock = 5;
         private int lineBlock = 6;
         private int tBlock = 7; **/

        SINGLE, SQUARE, REGULAR_L, BACKWARDS_L,
        ZIGZAG_HIGH_LEFT, ZIGZAG_HIGH_RIGHT, LINE, T;

        public static Block getRandomBlock() {
            int nextBlockType = (int)(Math.floor(Math.random()*7)+1);

            switch(nextBlockType) {
                case 0: return SINGLE;
                case 1: return SQUARE;
                case 2: return REGULAR_L;
                case 3: return BACKWARDS_L;
                case 4: return ZIGZAG_HIGH_LEFT;
                case 5: return ZIGZAG_HIGH_RIGHT;
                case 6: return LINE;
                case 7: return T;
                default: return SINGLE;
            }
        }
    }

    private Block currentBlockType = Block.SQUARE;
    private int currentBlockRotation = 0;//0, 90, 180, 270
    //this is how many degrees
    //clockwise it is
    private int currentBlockColor = (int)(Math.random() * 255 + 255 * 255) + 1;
    private int nextBlockColor = (int)(Math.random() * 255 + 255 * 255) + 1;
    public Block nextBlockType = Block.getRandomBlock();

    /** Game state **/
    public enum GameState {
        MAIN_MENU, MAIN_MENU_INSTRUCTIONS, MAIN_MENU_ABOUT, MAIN_MENU_SETTINGS,
        IN_GAME, PAUSED,
        GAME_OVER
    }

    public GameState gameState = GameState.MAIN_MENU;

    // public boolean mainMenu = false;
    // public boolean gameOver = false;
    // public boolean paused = false;

    /**  Input time constants
     * Ensures that buttons aren't clicked too quickly on accident
     **/
    private final int defaultLagTime = 400;
    private int leftLag  = defaultLagTime; // aka at least .2 seconds between lefts
    private int rightLag = defaultLagTime;
    private int upLag    = defaultLagTime * 2;
    private long lastLeftPressTime = 0;
    private long lastRightPressTime = 0;
    private long lastUpPressTime = 0;

    /**
     * Resets all variables so that the
     * game can start anew
     */
    private void reset() {
        row = new int[]{0, 0, 1, 1};
        col = new int[]{5, 4, 5, 4};
        blockArray = new int[levelwidth * levelheight];
        colours = new int[4];
        time = 0;
        score = 0;
        baseSpeed=20;
        inputSpeed=0;
        updateSpeed = 20;
        int level = 1;
        linesCleared = 0;
        currentBlockType = Block.SQUARE;
        currentBlockRotation = 0;
        currentBlockColor = (int)(Math.random() * 255 + 255 * 255) + 1;
        nextBlockColor = (int) (Math.random() * 255 + 255 * 255) + 1;
        nextBlockType = Block.getRandomBlock();

        for(int i= 0; i < 4;i++){
            colours[i] = getRandomColour();
        }

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
        long lastPausePressTime = 0;
    }

    /** blockArray getter and setter **/
    public int getBlock(int column, int row)             { return blockArray[column + row * levelwidth];  }
    private void setBlock(int column, int row, int value) { blockArray[column + row * levelwidth] = value; }
    public int getColour(int index) { return colours[index];}

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
                if(!selfcollision) {
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
                if(!selfcollision) {
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
                if(!selfcollision) {
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
        int[] oldColours = {0, 0, 0, 0};
        int[] newrow = {0, 0, 0, 0};
        int[] newcol = {0, 0, 0, 0};

        for(int i = 0; i < 4; i++) {
            oldrow[i] = row[i];
            oldcol[i] = col[i];
            oldColours[i] = colours[i];
        }

        if(currentBlockType == Block.SQUARE) { // square
            newrow = row;
            newcol = col;
            colours[0] = oldColours[3];
            colours[1] = oldColours[0];
            colours[2] = oldColours[1];
            colours[3] = oldColours[2];
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
    public void keyPressed(Input input) {
        for(int i = 0; i < 4; i++) {
            setBlock(col[i], row[i], 0);
        }

        int lagChange = 250;
        if (input == Input.LEFT &&  !leftCollision() && System.currentTimeMillis() - lastLeftPressTime > leftLag){ // Left clicked, won't collide, and enough time passed
            lastLeftPressTime = System.currentTimeMillis();
            leftLag -= lagChange; // allows user to hold left to slide left
            rightLag = defaultLagTime;
            upLag = defaultLagTime;

            for(int i = 0; i < 4; i++)
                col[i]--;

            vibrator.vibrate(LR_VIBRATE_DURATION);
        }
        if (input == Input.RIGHT && !rightCollision() && System.currentTimeMillis() - lastRightPressTime > rightLag) {
            rightLag -= lagChange; // allows user to hold right to slide right
            leftLag = defaultLagTime;
            upLag = defaultLagTime;
            lastRightPressTime = System.currentTimeMillis();

            for(int i = 0; i < 4; i++)
                col[i]++;

            vibrator.vibrate(LR_VIBRATE_DURATION);
        }
        if (input == Input.UP && System.currentTimeMillis() - lastUpPressTime > upLag) {
            upLag -= lagChange;
            rightLag = defaultLagTime;
            leftLag = defaultLagTime;
            lastUpPressTime = System.currentTimeMillis();

            if(upLag < 100)
                upLag = 100;

            rotateBlock();

            vibrator.vibrate(LR_VIBRATE_DURATION);
        }
        if (input == Input.DOWN && !bottomCollision()) {
            rightLag = defaultLagTime;
            leftLag = defaultLagTime;
            upLag = defaultLagTime;

            for(int i = 0; i < 4; i++)
                row[i]++;

            vibrator.vibrate(LR_VIBRATE_DURATION);
        }

        for(int i = 0; i < 4; i++)
            setBlock(col[i], row[i], currentBlockColor);
    }
    private boolean alreadyExecuted = false;
    public void setInitAngle() {if(!alreadyExecuted) {
        initYaw = motionSensor.getHeadAngles()[1];
        initPitch = motionSensor.getQuatAngles()[2];
        alreadyExecuted = true;
    }}
    /**
     * update block based on accelerometer data
     */
    public void motionSensorMove() {
        int leftCol = levelwidth;
        int rightCol = -1;
        for (int aCol : col) {
            if (aCol < leftCol) leftCol = aCol;
            if (aCol > rightCol) rightCol = aCol;
        }
        /**
         * track the yaw of your face based on cardboard sdk headtracker - this code is terrible and should be updated
         */
        float pi = 2 *(float) Math.PI; //2*PI is set to pi - don't let that confuse you, it makes calculations simpler
        float dAngleLim = pi/20;//How far you have to turn in order to move the block left or right. I made that pi/10 or 18 degrees in any direction
        float currYaw =  motionSensor.getHeadAngles()[1];
        float currPitch = motionSensor.getQuatAngles()[2];
        /**
         * track the yaw of your face based on cardboard sdk headtracker - this code is terrible and should be updated
         */
        if(currYaw<0 == initYaw>0){//behavior when you reach 0 and pi or -pi
            if(Math.abs(initYaw) > dAngleLim ) {
                if(initYaw>0 && pi-(initYaw + dAngleLim) > currYaw ) { //forgive me padre for I have sinned
                    keyPressed(Input.LEFT);
                    initYaw = currYaw;
                } else if(initYaw<0 && (pi*-1) - (initYaw - dAngleLim) < currYaw) {
                    keyPressed(Input.RIGHT);
                    initYaw = currYaw;
                }
            } else {//behavior when you are passing through 0 to pi or -pi to 0
                if(Math.abs(currYaw) + Math.abs(initYaw) >= dAngleLim){
                    if(initYaw>=0) {
                        keyPressed(Input.RIGHT);
                        initYaw = currYaw;
                    } else if(initYaw<0) {
                        keyPressed(Input.LEFT);
                        initYaw = currYaw;
                    }
                }
            }
        } else if(currYaw - initYaw >= dAngleLim) {
            keyPressed(Input.LEFT);
            initYaw = currYaw;
        } else if (currYaw - initYaw <= dAngleLim * -1 ) {
            keyPressed(Input.RIGHT);
            initYaw = currYaw;
        }
        if(Math.abs(currPitch) - Math.abs(initPitch) < dAngleLim*-1.3){
            inputSpeed = baseSpeed - 1;
            setDifficulty();
        } else {
            inputSpeed = 0;
            setDifficulty();
        }

        /*
        int blockwidth = rightCol - leftCol + 1;

        int currentCol = leftCol;
        int expectedCol = (int) map(motionSensor.getVelocities()[1], motionSensor.minYV/2, motionSensor.maxYV/2, 0, levelwidth - blockwidth);
        if(expectedCol < currentCol) {
            keyPressed(Input.LEFT);
        } else if(expectedCol > currentCol) {
            keyPressed(Input.RIGHT);
        } */
    }

    /**
     * From processing.js source
     * Re-maps a number from one range to another.
     * Numbers outside the range are not clamped to 0 and 1, because out-of-range values are often intentional and useful.
     *
     * @param {float} value        The incoming value to be converted
     * @param {float} istart       Lower bound of the value's current range
     * @param {float} istop        Upper bound of the value's current range
     * @param {float} ostart       Lower bound of the value's target range
     * @param {float} ostop        Upper bound of the value's target range
     */
    public double map(double value, double istart, double istop, double ostart, double ostop) {
        return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
    }

    /** sets your current block to what was shown and makes a
     * random next block
     */
    private void nextBlock() {

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
            Log.d(MainActivity.TAG, "game over");
            gameState = GameState.GAME_OVER; // gameOver = true;
        }

        currentBlockColor = nextBlockColor;
        currentBlockType = nextBlockType;
        currentBlockRotation = 0;
        nextBlockColor = (int) Math.floor(Math.random() * 255 + 255 * 255) + 1;
        nextBlockType = Block.getRandomBlock();
        for(int i = 0; i < colours.length; i++){
            colours[i] = getRandomColour();
        }
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
            boolean recentPress = System.currentTimeMillis() - lastLeftPressTime < defaultLagTime * 3 ||
                    System.currentTimeMillis() - lastRightPressTime < defaultLagTime * 3 ||
                    System.currentTimeMillis() - lastUpPressTime < defaultLagTime * 3;
            if(!recentPress) {
                score += 15 + linesCleared;
                nextBlock();
            }
        }

        for(int i = 0; i < 4; i++)
            setBlock(col[i], row[i], colours[i]);
    }

    /**
     * Makes the game go faster after you clear a line
     */
    private void setDifficulty() {
        baseSpeed = 20 - linesCleared;
        if(linesCleared >= 16) {
            baseSpeed = 4;
        }
        updateSpeed = baseSpeed - inputSpeed;
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
            if(filled) {
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
                if(!posReset) {
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

    private int getRandomColour(){
        if(Math.random() > 0.5){
            return bgColor;
        }else{
            return activeEyeBlockColor;
        }
    }

    public int[] getBlockColours(int index){
        int[] blockColours;
        if(colours[index] == bgColor){
            blockColours = new int[]{bgColor, activeEyeBlockColor};
        }else{
            blockColours = new int[]{activeEyeBlockColor, bgColor};
        }
        return blockColours;
    }

    /**
     * Called when the "action button" is pressed
     * Action button = Cardboard magnet, screen touch, or tilting phone up
     *
     * Rotates block in-game
     */
    public void isOn() {

    }
    public void actionButton() {
        if(gameState == GameState.IN_GAME) {
            // Always give user feedback
            vibrator.vibrate(100);
            keyPressed(Input.UP);
        } else if(gameState == GameState.GAME_OVER) {
            vibrator.vibrate(100);
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
     *
     * @param motionSensorModule accelerometer
     * @param vibrator for vibrating
     * @param globalColours globally used colours
     */
    public TetrisModel(MotionSensorModule motionSensorModule, Vibrator vibrator, int[] globalColours) {
        // accelerometer for detecting movement of the device
        motionSensor = motionSensorModule;

        this.vibrator = vibrator;

        //assign global colours
        activeEyeBlockColor = globalColours[0];
        bgColor = globalColours[1];
        fallenColour = globalColours[2];

        for(int i = 0; i < colours.length;i++){
            colours[i] = getRandomColour();
        }

        for(int r = 0; r < levelwidth; r++) {
            for(int c = 0; c < levelheight; c++) {
                blockArray[r + c * levelwidth] = 0;
            }
        }
    }
}
