package net.qwuke.unblyopia;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
/**
 * Created by Raphael on 9/6/2014.
 */
public class TetrisView extends View {

    private ShapeDrawable mDrawable;
    Paint paint = new Paint();

    long lastTimeMillis = 0;

    Canvas currCanvas = null;

    int blockSize = 20;
    int borderX = 200;
    int[] row = {0, 0, 1, 1};
    int[] col = {5, 4, 5, 4};
    int levelwidth = 10;
    int levelheight = 20;
    int[] blockArray = new int[levelwidth * levelheight];
    int time = 0;
    int score = 0;
    int updateSpeed = 20;
    int level = 1;
    int linesCleared = 0;
    // block constants
    int singleBlock = 0;
    int squareBlock = 1;
    int regularLBlock = 2;
    int backwardsLBlock = 3;
    int zigzagHighLeftBlock = 4;
    int zigzagLowLeftBlock = 5;
    int lineBlock = 6;
    int tBlock = 7;
    int currentBlockType = squareBlock;
    int currentBlockRotation = 0;//0, 90, 180, 270
                                //this is how many degrees
                                //clockwise it is
    int currentBlockColor = 0xff0000;
    int nextBlockColor = (int)(Math.random() * 255 + 255 * 255 * 255);
    int nextBlockType = (int)(Math.floor(Math.random()*7)+1);
    boolean mainMenu = false; //true;
    boolean gameOver = false;
    boolean paused = false;
    //boolean mobile = false;
//    textSize(21);
//    line(borderX, 0, borderX, 400);
    int leftLag = 200;
    int rightLag = 200;
    long lastLeftPressTime = 0;
    long lastRightPressTime = 0;
    long lastUpPressTime = 0;
    long lastPausePressTime = 0;
    
    private int getX(int column) {
        return column * blockSize;
    }

    private int getY(int roww) {
        return roww * 20;
    }

    private int getBlock(int column, int rowe) {
        return blockArray[column + rowe * levelwidth];
    }

    private void setBlock(int column, int rowe, int value) {
        blockArray[column + rowe * levelwidth] = value;
    }

    // draws the block
    private void drawShapes() {
        paint.setColor(0xffff00ff); // fill(255, 0, 0);
        paint.setStrokeWidth(10);
        currCanvas.drawText("HEY", .5f, .5f, paint);
        //Log.d("FILLER TEXT", "x: " + getX(0) + " y: " + getY(0));
        currCanvas.drawRect(getX(0)+5, getY(0)+5, getX(0)+5 + blockSize, getY(0)+5 + blockSize, paint);
        //stroke(0, 0, 0);
        for(int r = 0; r < levelheight; r++) {
            for(int c = 0; c < levelwidth; c++) {
                if(getBlock(c, r) != 0) {
                    int color = getBlock(c, r);
                    int blue = color % 255;
                    int green = ((color - blue)  % (255^2)) >> 8;
                    int red = (color - green - blue) >> 16;
                    paint.setColor(0xff00ff00);// fill(red, green, blue);
                    //Log.d("Filler text", "block at l: " + getX(c) + " t: " + getY(r));
                    currCanvas.drawRect(getX(c), getY(r), getX(c) + blockSize, getY(r) + blockSize, paint); //rect(getX(c), getY(r), blockSize, blockSize);
                }
            }
        }
        //rect(getX(col), getY(row), blockSize, blockSize);
    }

    // erases the block
    private void eraseShapes() {
        // background equal to a number between 0 and 255
        // depending on current block location and time
        int rowValue = (row[0] * blockSize +
                (time - 1) % updateSpeed) * 255 /
                (levelheight * blockSize);

        int colValue = (col[0] * blockSize +
                (time - 1 - updateSpeed / 2) %
                        updateSpeed) * 255 /
                (levelwidth * blockSize);

        paint.setColor((rowValue << 8) + (colValue << 4) + 20); //fill(rowValue, colValue, 20);
        // noStroke();

        // rect(borderX, 0, blockSize * levelwidth,
        //         blockSize * levelheight);
    }

    private boolean bottomCollision() {
        for(int i = 0; i < 4; i++) {

            int btmCollisionCol = col[i];
            int btmCollisionRow = row[i] + 1;

            //Log.d("FILLER TAG", "btmColCol = " + btmCollisionCol);
            //Log.d("FILLER TAG", "btmColRow = " + btmCollisionRow);

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

        if(currentBlockType == 1) { // square
            newrow = row;
            newcol = col;
        }
        else if(currentBlockType == 2) { // normal l
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
        else if(currentBlockType == 3) { // backwards l
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
        else if(currentBlockType == 4) { // zigzag
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
        else if(currentBlockType == 5) { // backwards zigzag
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
        else if(currentBlockType == 6) { // line
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
        else if(currentBlockType == 7) { // T
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

    public static final int LEFT = 65;
    public static final int RIGHT = 68;
    public static final int UP = 87;
    public static final int DOWN = 83;

    // update block based on keypresses
    public void keyPressed(int keyCode) {
        for(int i = 0; i < 4; i++) {
            setBlock(col[i], row[i], 0);
        }

        if ((keyCode == LEFT || keyCode == 65) &&
                !leftCollision() &&
                System.currentTimeMillis() - lastLeftPressTime > leftLag){
            lastLeftPressTime = System.currentTimeMillis();
            leftLag -= 150;
            rightLag = 200;
            for(int i = 0; i < 4; i++) {
                col[i]--;
            }
        }
        if ((keyCode == RIGHT || keyCode == 68) &&
                !rightCollision() &&
                System.currentTimeMillis() - lastRightPressTime > rightLag) {
            rightLag -= 150;
            leftLag = 200;
            lastRightPressTime = System.currentTimeMillis();
            for(int i = 0; i < 4; i++) {
                col[i]++;
            }
        }
        if ((keyCode == UP || keyCode == 87 /*||
                keyCode == CONTROL*/) &&
                System.currentTimeMillis() - lastUpPressTime > 200) {
            rightLag = 200;
            leftLag = 200;
            rotateBlock();
            lastUpPressTime = System.currentTimeMillis();
        }
        if ((keyCode == DOWN || keyCode == 83) &&
                !bottomCollision()) {
            rightLag = 200;
            leftLag = 200;
            for(int i = 0; i < 4; i++) {
                row[i]++;
            }
        }

        for(int i = 0; i < 4; i++) {
            setBlock(col[i], row[i], currentBlockColor);
        }
    }

    /*
    private void mobileKeyPressed() {
        int mouseKey = -1;
        if(mouseIsPressed && mobile &&
                mouseX > borderX && mouseY > 290) {

            if(mouseX < 263 && mouseY < 360) { // left
                mouseKey = LEFT;
                rect(borderX, 300, 63, 60);
            }
            if(mouseX > 263 && mouseX < 337 &&  // rotate
                    mouseY < 355) {
                rect(268, 290, 63, 60);
                mouseKey = UP;
            }
            if(mouseX > 337 && mouseY < 360) { // right
                mouseKey = RIGHT;
                rect(337, 300, 63, 60);
            }
            if(mouseY > 363) { // down
                mouseKey = DOWN;
                rect(borderX, 363, 200, 37);
            }

        }
        if (mouseKey != -1) {
            for(int i = 0; i < 4; i++) {
                setBlock(col[i], row[i], 0);
            }

            if (mouseKey == LEFT && !leftCollision() &&
                    millis() - lastLeftPressTime > leftLag){
                lastLeftPressTime = millis();
                leftLag -= 150;
                rightLag = 200;
                for(int i = 0; i < 4; i++) {
                    col[i]--;
                }
            }
            if (mouseKey == RIGHT && !rightCollision() &&
                    System.currentTimeMillis() - lastRightPressTime > rightLag) {
                rightLag -= 150;
                leftLag = 200;
                lastRightPressTime = System.currentTimeMillis();
                for(int i = 0; i < 4; i++) {
                    col[i]++;
                }
            }
            if (mouseKey == UP &&
                    System.currentTimeMillis() - lastUpPressTime > 200) {
                rightLag = 200;
                leftLag = 200;
                rotateBlock();
                lastUpPressTime = System.currentTimeMillis();
            }
            if (mouseKey == DOWN && !bottomCollision()) {
                rightLag = 200;
                leftLag = 200;
                for(int i = 0; i < 4; i++) {
                    row[i]++;
                }
            }

            for(int i = 0; i < 4; i++) {
                setBlock(col[i], row[i], currentBlockColor);
            }
        }
    }
    */

    // sets your current block to what was shown and makes a
    // random next block
    private void nextBlock() {
        if(nextBlockType == 1) { // square
            row = new int[]{0, 0, 1, 1};
            col = new int[]{5, 4, 5, 4};
        }
        else if(nextBlockType == 2) { // normal l
            row = new int[]{0, 1, 2, 2};
            col = new int[]{4, 4, 4, 5};
        }
        else if(nextBlockType == 3) { // backwards l
            row = new int[]{0, 1, 2, 2};
            col = new int[]{5, 5, 5, 4};
        }
        else if(nextBlockType == 4) { // zigzag
            row = new int[]{1, 1, 0, 0};
            col = new int[]{4, 5, 5, 6};
        }
        else if(nextBlockType == 5) { // backwards zigzag
            row = new int[]{0, 0, 1, 1};
            col = new int[]{4, 5, 5, 6};
        }
        else if(nextBlockType == 6) { // line
            row = new int[]{0, 1, 2, 3};
            col = new int[]{5, 5, 5, 5};
        }
        else if(nextBlockType == 7) { // T
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
        nextBlockType = (int) Math.floor(Math.random()*7)+1;
    }

    // move block down and check block under
    // if block underneath, goes to next block
    private void moveDown() {
        if (!bottomCollision()) {
            for(int i = 0; i < 4; i++) {
                setBlock(col[i], row[i], 0);
                row[i]++;
            }
        }
        else{
            score += 15 + linesCleared;
            nextBlock();
        }
        for(int i = 0; i < 4; i++) {
            setBlock(col[i], row[i], currentBlockColor);
        }
    }

    // draw the sidebar
    private void drawSide() {
        paint.setTextSize(21); //textSize(21);

        // sidebar bg
        paint.setColor((178 << 8) + (227 << 4) + 104); // fill(178, 227, 104);
        currCanvas.drawRect(0, 0, 199, 399, paint); // rect(0, 0, 199, 399);

        // sidebar textbox
        paint.setColor((245 << 8) + (228 << 4) + 245); // fill(245, 228, 245);
        currCanvas.drawRect(15, 10, 15 + 166, 10 + 200, paint); // rect(15, 10, 166, 200);

        // text
        paint.setColor(Color.BLACK); // fill(0, 0, 0);
        currCanvas.drawText("Time: " + time, 20, 30, paint);
        currCanvas.drawText("Score: " + score, 20, 59, paint);
        currCanvas.drawText("row: " + row, 20, 130, paint);
        currCanvas.drawText("col:   " + col, 20, 170, paint);
        currCanvas.drawText("lines: " + linesCleared, 20, 90, paint);
        currCanvas.drawText("rotation: " + currentBlockRotation, 20, 190, paint);
        currCanvas.drawText("block: " + currentBlockType, 20, 150, paint);

        // next block type box
        paint.setColor((245 << 8) + (218 << 4) + 81);// fill(245, 218, 81);
        currCanvas.drawRect(15, 210, 15 + 166, 210 + 120, paint);// rect(15, 210, 166, 120);
        paint.setColor((26 << 8) + (18 << 4) + 26);// fill(26, 18, 26);
        currCanvas.drawText("Next: " + nextBlockType, 20, 230, paint);

        // next block
        paint.setColor(Color.RED); // fill(255, 0, 0);
        currCanvas.drawRect(65, 260, 65 + 20, 260 + 20, paint);
        currCanvas.drawRect(65, 240, 65 + 20, 240 + 20, paint);
        if(nextBlockType == squareBlock) {
            currCanvas.drawRect(85, 240, 85 + 20, 240 + 20, paint);
            currCanvas.drawRect(85, 260, 85 + 20, 260 + 20, paint);
        }
        if(nextBlockType == regularLBlock) {
            currCanvas.drawRect(65, 280, 65 + 20, 280 + 20, paint);
            currCanvas.drawRect(85, 280, 85 + 20, 280 + 20, paint);
        }
        if(nextBlockType == backwardsLBlock) {
            currCanvas.drawRect(65, 280, 65 + 20, 280 + 20, paint);
            currCanvas.drawRect(45, 280, 45 + 20, 280 + 20, paint);
        }
        if(nextBlockType == zigzagHighLeftBlock) {
            currCanvas.drawRect(85, 240, 85 + 20, 240 + 20, paint);
            currCanvas.drawRect(45, 260, 45 + 20, 260 + 20, paint);
        }
        if(nextBlockType == zigzagLowLeftBlock) {
            currCanvas.drawRect(85, 260, 85 + 20, 260 + 20, paint);
            currCanvas.drawRect(45, 240, 45 + 20, 240 + 20, paint);
        }
        if(nextBlockType == lineBlock) {
            currCanvas.drawRect(65, 280, 65 + 20, 280 + 20, paint);
            currCanvas.drawRect(65, 300, 65 + 20, 300 + 20, paint);
        }
        if(nextBlockType == tBlock) {
            currCanvas.drawRect(45, 260, 45 + 20, 260 + 20, paint);
            currCanvas.drawRect(85, 260, 85 + 20, 260 + 20, paint);
        }

        // pause button
        if(paused == false) {
            paint.setColor((86 << 8) + (245 << 4) + 96); // fill(86, 245, 96);
            currCanvas.drawRect(15, 335, 15 + 166, 335 + 25, paint);
            paint.setColor((26 << 8) + (18 << 4) + 26); // fill(26, 18, 26);
            currCanvas.drawText("Pause", 66, 355, paint);
        }
        else {
            paint.setColor(Color.RED); // fill(255, 0, 0);
            currCanvas.drawRect(15, 335, 15 + 166, 335 + 25, paint);
            paint.setColor(Color.WHITE); // fill(255, 255, 255);
            currCanvas.drawText("Continue", 55, 355, paint);
        }

        // name
        paint.setColor((26 << 8) + (18 << 4) + 26); // fill(26, 18, 26);
        paint.setTextSize(17); // textSize(17);
        currCanvas.drawText("By Raphael Kats", 72, 394, paint);
    }

// draw GAME OVER on the screen
    private void drawGameOverScreen() {
        paint.setColor(Color.WHITE);// fill(255, 255, 255);
        paint.setTextSize(40); // textSize(40);
        currCanvas.drawText("GAME\nOVER", 235, 95, paint);
    }

// makes the game go faster after you clear a line
    private void setDifficulty() {
        updateSpeed = 21 - linesCleared;
        if(linesCleared >= 19) {
            updateSpeed = 2;
        }
    }

// removes lines
// if lines were removed, updates score and difficulty and
// goes to the next block
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

    private void drawMainMenu() {
        // bg
        paint.setColor((157 << 8) + (184 << 4) + 51); // fill(157, 184, 51);
        currCanvas.drawRect(0, 0, 400, 400, paint); // rect(0, 0, 400, 400);

        // "tetris"
        int tetrisR = (int) (255 * Math.sin(System.currentTimeMillis()/50) + 255);
        int tetrisG = (int) (255 * Math.cos(System.currentTimeMillis()/50) + 255);
        int tetrisB = (int) (100 + 100 * Math.cos(System.currentTimeMillis()/50));
        paint.setColor((tetrisR << 8) + (tetrisG << 4) + tetrisB);
        paint.setTextSize(91); // textSize(91);
        currCanvas.drawText("tetris", 79, 92, paint);

        // Regular mode
        paint.setColor((255 << 8) + (213 << 4) + 0); // fill(255, 213, 0);
        currCanvas.drawRect(114, 167, 114 + 182, 167 + 41, paint);
        paint.setTextSize(37); // textSize(37);
        paint.setColor(Color.BLACK); // fill(0, 0, 0);
        currCanvas.drawText("Regular", 142, 200, paint);

        // Mobile mode
        paint.setColor((255 << 8) + (213 << 4) + 0); // fill(255, 213, 0);
        currCanvas.drawRect(114, 229, 114 + 182, 229 + 41, paint);
        paint.setColor(Color.BLACK); // fill(0, 0, 0);
        currCanvas.drawText("Mobile", 155, 262, paint);


    }

    // draws arrow boxes for mobile browser support
    /*
    private void drawMobile() {
        fill(255, 255, 255, 40);
        noStroke();

        //boxes
        rect(borderX, 300, 63, 60); // left
        rect(268, 290, 63, 60); // rotate
        rect(337, 300, 63, 60); // right
        rect(borderX, 363, 200, 37); // down

        fill(0, 0, 0, 40);
        // arrows
        triangle(210, 330, 250, 310, 250, 350); // left
        triangle(270, 330, 300, 293, 330, 330); // rotate
        triangle(390, 330, 350, 310, 350, 350); // right
        triangle(270, 370, 330, 370, 300, 390); // down

        stroke(0, 0, 0);
    }
    */

    // main loop
    private void draw() {
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
                    //eraseShapes();
                    drawShapes();
                    //drawSide();

                    //if(mobile) {
                    //    drawMobile();
                    //    mobileKeyPressed();
                    //} else {
                        //keyPressed();
                    //}

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
                    // mobile
                    if(mouseY > 229 && mouseY < 270) {
                        rect(114, 229, 182, 41);
                        mobile = true;
                        mainMenu = false;
                        paused = false;
                    }
                }

            }
            */
        }
    }

    private void keyReleased() {
        //if(keyCode == 80) {
        //    paused = !paused;
        //}
    }

    public TetrisView(Context context) {
        super(context);
        for(int r = 0; r < levelwidth; r++) {
            for(int c = 0; c < levelheight; c++) {
                blockArray[r + c * levelwidth] = 0;
            }
        }
    }

    protected void onDraw(Canvas canvas) {
        //if(System.currentTimeMillis() > 33 + lastTimeMillis) {
        //    update();
        //    lastTimeMillis = System.currentTimeMillis();
        //}

        currCanvas = canvas;
        //paint.setColor(0xFF0000FF);
        //canvas.drawRect(0.0f, 0.2f, 40.0f, 40.0f, paint);
        draw();

        // forces redraw
        super.postInvalidate();
    }

    /*
    private void update() {
    }
    private void draw() {
        int x = 10;
        int y = 50;
        int width = 300;
        int height = 50;
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        canvas.drawRect(x, y, x+width, y+height, paint);
        paint.setStrokeWidth(0);
        paint.setColor(Color.CYAN);
        canvas.drawRect(x+2,y+30,x+width-2,y+height-2, paint);
        paint.setColor(Color.YELLOW);
        canvas.drawRect(x+2,y+2,x+width-2,y+height-10, paint);

        //mDrawable = new ShapeDrawable(new RectShape());
        //mDrawable.getPaint().setColor(0x111111);
        //mDrawable.setBounds(x, y, x + width, y + height);

        //mDrawable.draw(canvas);
    }
    */
}
