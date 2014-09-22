package net.qwuke.unblyopia;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.os.Vibrator;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * I'd recommend looking at this class with all the methods minimized
 * if you don't want a mile-long scrollbar
 *
 * Structure:
 * -get constructed
 * -call onDraw forever (main game loop)
 * --update
 * ---in game
 * ----updates based on input are immediate
 * ----every 20 frames the block moves down
 * ----check for game over
 * ---out of game
 * ----check if user's interacted with anything
 * --draw everything
 *
 * Created by Raphael on 9/6/2014.
 */
public class TetrisView extends View {

    private ShapeDrawable mDrawable;
    private Paint paint = new Paint();

    private long lastTimeMillis = 0;

    private Canvas currCanvas = null;
    private MotionSensorModule motionSensor;
    private Vibrator vibrator;

    // dimension variables
    private int width;
    private int height;
    private int blockSize;
    private int xSideOffset;
    private int vertPadding;

    // used in keyPress method

    public enum Input {
        LEFT, RIGHT, UP, DOWN
    }

    private static final int VIBRATE_DURATION = 100;

    // colors
    public static int activeEyeBlockColor = 0xff818CC7; // light blue
    public static int dormantEyeBlockColor = 0xff182461; // dark blue
    public static int bgColor = 0xff101B52; // darker blue

    //int borderX = 200;
    private int[] row = {0, 0, 1, 1};
    private int[] col = {5, 4, 5, 4};
    private int levelwidth = 10;
    private int levelheight = 20;
    private int[] blockArray = new int[levelwidth * levelheight];
    private int time = 0;
    private int score = 0;
    private int updateSpeed = 20;
    private int level = 1;
    private int linesCleared = 0;

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
    private int currentBlockColor = 0xffff0000;
    private int nextBlockColor = 0xff000000 + (int)(Math.random() * 255 + 255 * 255 * 255);
    private Block nextBlockType = Block.getRandomBlock();

    /** Game scenes **/
    private boolean mainMenu = false;
    private boolean gameOver = false;
    private boolean paused = false;

    /**  Input time constants
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
        currentBlockColor = 0xffff0000;
        nextBlockColor = 0xff000000 + (int)(Math.random() * 255 + 255 * 255 * 255);
        nextBlockType = Block.getRandomBlock();

        mainMenu = false;
        gameOver = false;
        paused = false;
        leftLag = defaultLagTime;
        rightLag = defaultLagTime;
        upLag = defaultLagTime *2;
        lastLeftPressTime = 0;
        lastRightPressTime = 0;
        lastUpPressTime = 0;
        lastPausePressTime = 0;
    }

    /** Turn row and column info into x and y values **/
    private int getX(int column) { return column * blockSize; }
    private int getY(int row)    { return row    * blockSize; }

    /** blockArray getter and setter **/
    private int getBlock(int column, int row)             { return blockArray[column + row * levelwidth];  }
    private void setBlock(int column, int row, int value) { blockArray[column + row * levelwidth] = value; }

    /**
     * Draws the blockArray onto the screen
     */
    private void drawShapes() {

        // draw dormant blocks
        for(int r = 0; r < levelheight; r++) {
            for(int c = 0; c < levelwidth; c++) {
                if(getBlock(c, r) != 0) {
                    paint.setColor(dormantEyeBlockColor);
                    currCanvas.drawRect(xSideOffset + getX(c), getY(r) + vertPadding, xSideOffset + getX(c) + blockSize, getY(r) + blockSize + vertPadding, paint);

                    paint.setColor(activeEyeBlockColor);
                    currCanvas.drawRect(xSideOffset + getX(c) + width/2, getY(r) + vertPadding, xSideOffset + getX(c) + blockSize + width/2, getY(r) + blockSize + vertPadding, paint);
                }
            }
        }

        // draw active eye block colors
        for(int i = 0; i < row.length; i++) {
            int r = row[i];
            int c = col[i];
            if(getBlock(c, r) != 0) {
                paint.setColor(activeEyeBlockColor);
                currCanvas.drawRect(xSideOffset + getX(c), getY(r) + vertPadding, xSideOffset + getX(c) + blockSize, getY(r) + blockSize + vertPadding, paint);

                paint.setColor(dormantEyeBlockColor);
                currCanvas.drawRect(xSideOffset + getX(c) + width/2, getY(r) + vertPadding, xSideOffset + getX(c) + blockSize + width/2, getY(r) + blockSize + vertPadding, paint);
            }
        }
    }

    /**
     * Erases the screen
     */
    private void eraseShapes() {
        paint.setColor(bgColor);
        currCanvas.drawRect(xSideOffset, vertPadding, xSideOffset + blockSize*levelwidth, height - vertPadding, paint);
        currCanvas.drawRect(xSideOffset + width/2, vertPadding, xSideOffset + blockSize*levelwidth + width/2, height - vertPadding, paint);
    }

    /**
     * Checks whether the current shape will collide with something if it goes down one row
     */
    private boolean bottomCollision() {
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
    public void keyPressed(Input input) {
        for(int i = 0; i < 4; i++)
            setBlock(col[i], row[i], 0);

        if (input == Input.LEFT &&  !leftCollision() && System.currentTimeMillis() - lastLeftPressTime > leftLag){ // Left clicked, won't collide, and enough time passed
            lastLeftPressTime = System.currentTimeMillis();
            leftLag -= lagChange; // allows user to hold left to slide left
            rightLag = defaultLagTime;
            upLag = defaultLagTime;

            for(int i = 0; i < 4; i++)
                col[i]--;

            vibrator.vibrate(VIBRATE_DURATION);
        }
        if (input == Input.RIGHT && !rightCollision() && System.currentTimeMillis() - lastRightPressTime > rightLag) {
            rightLag -= lagChange; // allows user to hold right to slide right
            leftLag = defaultLagTime;
            upLag = defaultLagTime;
            lastRightPressTime = System.currentTimeMillis();

            for(int i = 0; i < 4; i++)
                col[i]++;

            vibrator.vibrate(VIBRATE_DURATION);
        }
        if (input == Input.UP && System.currentTimeMillis() - lastUpPressTime > upLag) {
            upLag -= lagChange;
            rightLag = defaultLagTime;
            leftLag = defaultLagTime;
            lastUpPressTime = System.currentTimeMillis();

            if(upLag < 100)
                upLag = 100;

            rotateBlock();

            vibrator.vibrate(VIBRATE_DURATION);
        }
        if (input == Input.DOWN && !bottomCollision()) {
            rightLag = defaultLagTime;
            leftLag = defaultLagTime;
            upLag = defaultLagTime;

            for(int i = 0; i < 4; i++)
                row[i]++;

            vibrator.vibrate(VIBRATE_DURATION);
        }

        for(int i = 0; i < 4; i++)
            setBlock(col[i], row[i], currentBlockColor);
    }

    /** sets your current block to what was shown and makes a
     * random next block
     */
    private void nextBlock() {

        // swap colors, might help both eyes train
        int temp                 = dormantEyeBlockColor;
            dormantEyeBlockColor = activeEyeBlockColor;
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
            gameOver = true;
        }

        currentBlockColor = nextBlockColor;
        currentBlockType = nextBlockType;
        currentBlockRotation = 0;
        nextBlockColor = (int) Math.floor(Math.random() * 255 + 255 * 255 * 255);
        nextBlockType = Block.getRandomBlock();
    }

    /** move block down and check block under
     * if block underneath and no recent keypresses, goes to next block
     */
    private void moveDown() {
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
            setBlock(col[i], row[i], currentBlockColor);
    }

    /**
     * Method to make it easier to change the paint color
     */
    private void fill(int r, int g, int b) {
        paint.setColor((256 << 12) + (r << 8) + (g << 4) + b);
    }

    /**
     * Method to fill the screen with a color
     */
    private void background(int r, int g, int b) {
        int origColor = paint.getColor();

        fill(r, g, b);
        currCanvas.drawRect(0, 0, width, height, paint);

        paint.setColor(origColor);
    }

    /**
     * Draws the sidebar
     * Not yet added
     */
    private void drawSide() {
        paint.setTextSize(21);

        // sidebar bg
        fill(178, 227, 104);
        currCanvas.drawRect(0, 0, 199, 399, paint);

        // sidebar textbox
        fill(245, 228, 245);
        currCanvas.drawRect(15, 10, 15 + 166, 10 + 200, paint);

        // text
        paint.setColor(Color.BLACK);
        currCanvas.drawText("Time: " + time, 20, 30, paint);
        currCanvas.drawText("Score: " + score, 20, 59, paint);
        currCanvas.drawText("row: " + row, 20, 130, paint);
        currCanvas.drawText("col:   " + col, 20, 170, paint);
        currCanvas.drawText("lines: " + linesCleared, 20, 90, paint);
        currCanvas.drawText("rotation: " + currentBlockRotation, 20, 190, paint);
        currCanvas.drawText("block: " + currentBlockType, 20, 150, paint);

        // next block type box
        fill(245, 218, 81);
        currCanvas.drawRect(15, 210, 15 + 166, 210 + 120, paint);
        fill(26, 18, 26);
        currCanvas.drawText("Next: " + nextBlockType, 20, 230, paint);

        // next block
        paint.setColor(Color.RED);
        currCanvas.drawRect(65, 260, 65 + 20, 260 + 20, paint);
        currCanvas.drawRect(65, 240, 65 + 20, 240 + 20, paint);
        if(nextBlockType == Block.SQUARE) {
            currCanvas.drawRect(85, 240, 85 + 20, 240 + 20, paint);
            currCanvas.drawRect(85, 260, 85 + 20, 260 + 20, paint);
        }
        if(nextBlockType == Block.REGULAR_L) {
            currCanvas.drawRect(65, 280, 65 + 20, 280 + 20, paint);
            currCanvas.drawRect(85, 280, 85 + 20, 280 + 20, paint);
        }
        if(nextBlockType == Block.BACKWARDS_L) {
            currCanvas.drawRect(65, 280, 65 + 20, 280 + 20, paint);
            currCanvas.drawRect(45, 280, 45 + 20, 280 + 20, paint);
        }
        if(nextBlockType == Block.ZIGZAG_HIGH_LEFT) {
            currCanvas.drawRect(85, 240, 85 + 20, 240 + 20, paint);
            currCanvas.drawRect(45, 260, 45 + 20, 260 + 20, paint);
        }
        if(nextBlockType == Block.ZIGZAG_HIGH_RIGHT) {
            currCanvas.drawRect(85, 260, 85 + 20, 260 + 20, paint);
            currCanvas.drawRect(45, 240, 45 + 20, 240 + 20, paint);
        }
        if(nextBlockType == Block.LINE) {
            currCanvas.drawRect(65, 280, 65 + 20, 280 + 20, paint);
            currCanvas.drawRect(65, 300, 65 + 20, 300 + 20, paint);
        }
        if(nextBlockType == Block.T) {
            currCanvas.drawRect(45, 260, 45 + 20, 260 + 20, paint);
            currCanvas.drawRect(85, 260, 85 + 20, 260 + 20, paint);
        }

        // pause button
        if(paused == false) {
            fill(86, 245, 96);
            currCanvas.drawRect(15, 335, 15 + 166, 335 + 25, paint);
            fill(26, 18, 26);
            currCanvas.drawText("Pause", 66, 355, paint);
        }
        else {
            paint.setColor(Color.RED);
            currCanvas.drawRect(15, 335, 15 + 166, 335 + 25, paint);
            paint.setColor(Color.WHITE);
            currCanvas.drawText("Continue", 55, 355, paint);
        }

        // name
        fill(26, 18, 26);
        paint.setTextSize(17);
        currCanvas.drawText("By Raphael and Tristan", 72, 394, paint);
    }

    /**
     * Draw GAME OVER on the screen
     */
    private void drawGameOverScreen() {
        background(255, 255, 255);

        paint.setColor(Color.BLACK);
        paint.setTextSize(40);
        currCanvas.drawText("  GAME OVER", width/6, height/6, paint);
        currCanvas.drawText("  GAME OVER", width/6 + width/2, height/6, paint);

        currCanvas.drawText("SCORE: " + score, width/6, height/6 + height/8, paint);
        currCanvas.drawText("SCORE: " + score, width/6 + width/2, height/6 + height/8, paint);

        currCanvas.drawText(" Act to restart", width/6, height/6 + height/4, paint);
        currCanvas.drawText(" Act to restart", width/6 + width/2, height/6 + height/4, paint);
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
    private void removeLines() {
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
     * Draws the main menu
     * Not added yet
     */
    private void drawMainMenu() {
        background(157, 184, 51);

        // "tetris"
        int tetrisR = (int) (255 * Math.sin(System.currentTimeMillis()/50) + 255);
        int tetrisG = (int) (255 * Math.cos(System.currentTimeMillis()/50) + 255);
        int tetrisB = (int) (100 + 100 * Math.cos(System.currentTimeMillis()/50));
        fill(tetrisR, tetrisG, tetrisB);
        paint.setTextSize(91); // textSize(91);
        currCanvas.drawText("tetris", 79/400*width/2, 92/400*height, paint);
        currCanvas.drawText("tetris", 79/400*width/2 + width/2, 92/400*height, paint);

        // Regular mode
        fill(255, 213, 0);
        currCanvas.drawRect(114/400*width/2, 167/400*height, (114 + 182)/400*width/2, (167 + 41)/400*height, paint);
        currCanvas.drawRect(114/400*width/2+width/2, 167/400*height, (114 + 182)/400*width/2+width/2, (167 + 41)/400*height, paint);
        paint.setTextSize(37); // textSize(37);
        paint.setColor(Color.BLACK);
        currCanvas.drawText("Begin", 142/400*width/2, 200/400*height, paint);
        currCanvas.drawText("Begin", 142/400*width/2+width/2, 200/400*height, paint);
    }


    /**
     * Main loop
     * Gets called every cycle
     * Updates things and draws them
     *
     * @param canvas, required for drawing to the screen
     */
    protected void onDraw(Canvas canvas) {
        // so canvas doesn't have to be passed as a parameter
        currCanvas = canvas;

        // actual draw and update function
        /* // pause if focus lost
        if(focused == false) {
            //paused = true;
        }
        */

        /* // pause when pause button pressed
        if(mouseIsPressed &&
                System.currentTimeMillis() - lastPausePressTime > 500 &&
                mouseX > 15 && mouseX < 181 &&
                mouseY > 335 && mouseY < 360)
        {
            paused = !paused;
            lastPausePressTime = System.currentTimeMillis();
        }
        */

        if(!mainMenu) { // in-game
            if(!paused) {
                if(!gameOver) {
                    eraseShapes();
                    drawShapes();
                    //drawSide();

                    if(bottomCollision()) {
                        removeLines();
                    }

                    if(time % updateSpeed == 0) {
                        moveDown();
                    }

                    time++;
                }
                else { // if game over
                    drawGameOverScreen();
                }
            }
            else { // if paused
                drawSide();
            }
        } else { // in Main Menu
            drawMainMenu();

            /*
            if(mouseIsPressed) {
                if(mouseX > 114 && mouseX < 296) {
                    // regular
                    if(mouseY > 167 && mouseY < 208) {
                        rect(114, 167, 182, 41);
                        mainMenu = false;
                        paused = false;
                    }
                }
            }
            */
        }

        drawMotion();

        // force redraw
        super.postInvalidate();
    }

    /**
     * Onscreen acceleration debugging:
     * Draws the x, y, and z velocities
     */
    public void drawMotion() {
        paint.setColor(0xffff0000);
        paint.setTextSize(40);

        currCanvas.drawText("x: " + motionSensor.getVelocities()[0], 79/400*width/2, height/6, paint);
        currCanvas.drawText("y: " + motionSensor.getVelocities()[1], 79/400*width/2, height/6 + height/10, paint);
        currCanvas.drawText("z: " + motionSensor.getVelocities()[2], 79 / 400 * width / 2, height / 6 + height / 5, paint);
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
        if(!mainMenu) { // in-game
            if(!paused) {
                if(!gameOver) {
                    // Always give user feedback
                    vibrator.vibrate(100);
                    keyPressed(Input.UP);
                }
                else { // if game over
                    // Always give user feedback
                    vibrator.vibrate(100);
                    reset();
                }
            }
            else { // if paused
                // unpause
            }
        } else { // in Main Menu
            // start
        }
    }

    /**
     * Constructor
     * Instantiates variables
     *
     * @param context Activity, required of all views
     * @param motionSensorModule accelerometer
     * @param vibrator for vibrating
     */
    public TetrisView(Context context, MotionSensorModule motionSensorModule, Vibrator vibrator) {
        super(context);

        // accelerometer for detecting movement of the device
        motionSensor = motionSensorModule;
        this.vibrator = vibrator;

        // get screen width and height
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        // variables reliant on screen width and height
        vertPadding = height/4;
        blockSize = (height - 2*vertPadding) / 20;
        xSideOffset = width/4 - blockSize*levelwidth/2;

        for(int r = 0; r < levelwidth; r++) {
            for(int c = 0; c < levelheight; c++) {
                blockArray[r + c * levelwidth] = 0;
            }
        }
    }
}
