package net.qwuke.unblyopia;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.ShapeDrawable;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import net.qwuke.unblyopia.TetrisModel.Block;

/**
 * Created by RAPHAEL on 9/22/2014.
 */
public class TetrisDrawer {

    private Canvas currCanvas;
    private ShapeDrawable mDrawable;
    private Paint paint = new Paint();
    private TetrisModel tm;

    // dimension variables
    private int width;
    private int height;
    private int blockSize;
    private int xSideOffset;
    private int vertPadding;



    /** Turn row and column info into x and y values **/
    private int getX(int column) { return column * blockSize; }
    private int getY(int row)    { return row    * blockSize; }

    /**
     * Draws the blockArray onto the screen
     */
    public void drawShapes() {

        // draw fallen blocks
        for(int r = 0; r < tm.levelheight; r++) {
            for(int c = 0; c < tm.levelwidth; c++) {
                int color = tm.getBlock(c, r);
                if(color != 0) {
                    paint.setColor(tm.rightEyeBlockColor + color);
                    currCanvas.drawRect(xSideOffset + getX(c), getY(r) + vertPadding, xSideOffset + getX(c) + blockSize, getY(r) + blockSize + vertPadding, paint);

                    paint.setColor(tm.activeEyeBlockColor + color);
                    currCanvas.drawRect(xSideOffset + getX(c) + width/2, getY(r) + vertPadding, xSideOffset + getX(c) + blockSize + width/2, getY(r) + blockSize + vertPadding, paint);
                }
            }
        }

        // draw active blocks
        for(int i = 0; i < tm.row.length; i++) {
            int r = tm.row[i];
            int c = tm.col[i];
            int color = tm.getBlock(c, r);
            if(color != 0) {
                // clear active left block
                paint.setColor(tm.bgColor);
                currCanvas.drawRect(xSideOffset + getX(c), getY(r) + vertPadding, xSideOffset + getX(c) + blockSize, getY(r) + blockSize + vertPadding, paint);

                // draw active left block
                paint.setColor(tm.activeEyeBlockColor + color);
                currCanvas.drawRect(xSideOffset + getX(c), getY(r) + vertPadding, xSideOffset + getX(c) + blockSize, getY(r) + blockSize + vertPadding, paint);

                // clear active right block
                paint.setColor(tm.bgColor);
                currCanvas.drawRect(xSideOffset + getX(c) + width/2, getY(r) + vertPadding, xSideOffset + getX(c) + blockSize + width/2, getY(r) + blockSize + vertPadding, paint);

                // draw active left block
                paint.setColor(tm.rightEyeBlockColor + color);
                currCanvas.drawRect(xSideOffset + getX(c) + width/2, getY(r) + vertPadding, xSideOffset + getX(c) + blockSize + width/2, getY(r) + blockSize + vertPadding, paint);
            }
        }
    }



    /**
     * Erases the screen
     */
    public void eraseShapes() {
        paint.setColor(tm.bgColor);
        currCanvas.drawRect(xSideOffset, vertPadding, xSideOffset + blockSize*tm.levelwidth, height - vertPadding, paint);
        currCanvas.drawRect(xSideOffset + width/2, vertPadding, xSideOffset + blockSize*tm.levelwidth + width/2, height - vertPadding, paint);
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
        currCanvas.drawText("Time: " + tm.time, 20, 30, paint);
        currCanvas.drawText("Score: " + tm.score, 20, 59, paint);
        currCanvas.drawText("row: " + tm.row, 20, 130, paint);
        currCanvas.drawText("col:   " + tm.col, 20, 170, paint);
        currCanvas.drawText("lines: " + tm.linesCleared, 20, 90, paint);
        // currCanvas.drawText("rotation: " + tm.currentBlockRotation, 20, 190, paint);
        // currCanvas.drawText("block: " + tm.currentBlockType, 20, 150, paint);

        // next block type box
        fill(245, 218, 81);
        currCanvas.drawRect(15, 210, 15 + 166, 210 + 120, paint);
        fill(26, 18, 26);
        currCanvas.drawText("Next: " + tm.nextBlockType, 20, 230, paint);

        // next block
        paint.setColor(Color.RED);
        currCanvas.drawRect(65, 260, 65 + 20, 260 + 20, paint);
        currCanvas.drawRect(65, 240, 65 + 20, 240 + 20, paint);
        if(tm.nextBlockType == Block.SQUARE) {
            currCanvas.drawRect(85, 240, 85 + 20, 240 + 20, paint);
            currCanvas.drawRect(85, 260, 85 + 20, 260 + 20, paint);
        }
        if(tm.nextBlockType == Block.REGULAR_L) {
            currCanvas.drawRect(65, 280, 65 + 20, 280 + 20, paint);
            currCanvas.drawRect(85, 280, 85 + 20, 280 + 20, paint);
        }
        if(tm.nextBlockType == Block.BACKWARDS_L) {
            currCanvas.drawRect(65, 280, 65 + 20, 280 + 20, paint);
            currCanvas.drawRect(45, 280, 45 + 20, 280 + 20, paint);
        }
        if(tm.nextBlockType == Block.ZIGZAG_HIGH_LEFT) {
            currCanvas.drawRect(85, 240, 85 + 20, 240 + 20, paint);
            currCanvas.drawRect(45, 260, 45 + 20, 260 + 20, paint);
        }
        if(tm.nextBlockType == Block.ZIGZAG_HIGH_RIGHT) {
            currCanvas.drawRect(85, 260, 85 + 20, 260 + 20, paint);
            currCanvas.drawRect(45, 240, 45 + 20, 240 + 20, paint);
        }
        if(tm.nextBlockType == Block.LINE) {
            currCanvas.drawRect(65, 280, 65 + 20, 280 + 20, paint);
            currCanvas.drawRect(65, 300, 65 + 20, 300 + 20, paint);
        }
        if(tm.nextBlockType == Block.T) {
            currCanvas.drawRect(45, 260, 45 + 20, 260 + 20, paint);
            currCanvas.drawRect(85, 260, 85 + 20, 260 + 20, paint);
        }

        // pause button
        if(tm.gameState == TetrisModel.GameState.IN_GAME) {
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
    public void drawGameOverScreen() {
        // background(255, 255, 255);

        paint.setColor(Color.WHITE);
        paint.setTextSize(24);
        currCanvas.drawText("GAME OVER", width/6, height/6  + height/8, paint);
        currCanvas.drawText("GAME OVER", width/6 + width/2, height/6  + height/8, paint);

        currCanvas.drawText("SCORE: " + tm.score, width/6, height/6 + 2*height/8, paint);
        currCanvas.drawText("SCORE: " + tm.score, width/6 + width/2, height/6 + 2*height/8, paint);

        currCanvas.drawText("Lines Cleared: " + tm.linesCleared, width/6, height/6 + 2*height/8, paint);
        currCanvas.drawText("Lines Cleared: " + tm.linesCleared, width/6 + width/2, height/6 + 2*height/8, paint);

        currCanvas.drawText("Act to restart", width/6, height/6 + 3*height/8, paint);
        currCanvas.drawText("Act to restart", width/6 + width/2, height/6 + 3*height/8, paint);
    }



    /**
     * Draws the main menu
     */
    public void drawMainMenu() {
        background(157, 184, 51);

        // "tetris"
        int tetrisR = (int) (255.0 * Math.sin(System.currentTimeMillis()/50.0) + 255);
        int tetrisG = (int) (255.0 * Math.cos(System.currentTimeMillis()/50.0) + 255);
        int tetrisB = (int) (100 + 100.0 * Math.cos(System.currentTimeMillis()/50));
        fill(tetrisR, tetrisG, tetrisB);

        paint.setColor(Color.WHITE);
        paint.setTextSize(36);
        currCanvas.drawText("Delaze", 135.0f/400*width/2, 180.0f/400*height, paint);
        currCanvas.drawText("Delaze", 135.0f/400*width/2 + width/2.0f, 180.0f/400*height, paint);

        // Regular mode
        fill(255, 213, 0);
        currCanvas.drawRect(114.0f/400*width/2, 167.0f/400*height, (114 + 182)/400.0f*width/2, (167 + 41)/400.0f*height, paint);
        currCanvas.drawRect(114.0f/400*width/2+width/2.0f, 167/400*height, (114 + 182)/400.0f*width/2+width/2.0f, (167 + 41)/400.0f*height, paint);
        paint.setTextSize(24);
        paint.setColor(Color.CYAN);
        currCanvas.drawText("Click to begin", 127.0f/400*width/2, 220.0f/400*height, paint);
        currCanvas.drawText("Click to begin", 127.0f/400*width/2+width/2.0f, 220.0f/400*height, paint);
    }



    /**
     * Onscreen acceleration debugging:
     * Draws the x, y, and z velocities
     */
    public void drawMotion() {
        fill(255, 0, 0);
        paint.setTextSize(40);

        currCanvas.drawText("x: " + tm.motionSensor.getVelocities()[0], 79/400*width/2, height/6, paint);
        currCanvas.drawText("y: " + tm.motionSensor.getVelocities()[1], 79/400*width/2, height/6 + height/10, paint);
        currCanvas.drawText("z: " + tm.motionSensor.getVelocities()[2], 79 / 400 * width / 2, height / 6 + height / 5, paint);
    }

    public TetrisDrawer(WindowManager wm, TetrisModel tm) {
        this.tm = tm;

        // get screen width and height
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        // variables reliant on screen width and height
        vertPadding = height/4;
        blockSize = (height - 2*vertPadding) / 20;
        xSideOffset = width/4 - blockSize*tm.levelwidth/2;
    }

    public void setCanvas(Canvas c) {
        currCanvas = c;
    }
}
