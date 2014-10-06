/**
 * Created by RAPHAEL on 9/22/2014.
 */
public class TetrisDrawer {
    private TetrisModel tm;

    // dimension variables
    // private int width;
    // private int height;
    private int blockSize;
    private int xSideOffset;
    private int vertPadding;
    private int xRightSideOffset;


    /** Turn row and column info into x and y values **/
    private int getX(int column) { return column * blockSize; }
    private int getY(int row)    { return row    * blockSize; }

    /**
     * Draws the BlockArray onto the screen
     */
    public void drawShapes() {

        // draw fallen Blocks
        for(int r = 0; r < tm.levelheight; r++) {
            for(int c = 0; c < tm.levelwidth; c++) {
                int colour = tm.getBlock(c, r);
                if(colour != 0) {
                    fill(tm.rightEyeBlockColor + colour);
                    fill(red(colour), green(colour), blue(colour), alpha(tm.rightEyeBlockColor));
                    rect(xSideOffset + getX(c), getY(r) + vertPadding, blockSize, blockSize); 

                    fill(tm.activeEyeBlockColor + colour);
                    fill(red(colour), green(colour), blue(colour), alpha(tm.activeEyeBlockColor));
                    rect(xRightSideOffset + getX(c), getY(r) + vertPadding, blockSize, blockSize);
                }
            }
        }

        // draw active Blocks
        for(int i = 0; i < tm.row.length; i++) {
            int r = tm.row[i];
            int c = tm.col[i];
            int colour = tm.getBlock(c, r);
            if(colour != 0) {
                // clear active left Block
                fill(tm.bgColor);
                rect(xSideOffset + getX(c), getY(r) + vertPadding, blockSize, blockSize);


                // draw active left Block
                fill(tm.activeEyeBlockColor + colour);
                fill(red(colour), green(colour), blue(colour), alpha(tm.activeEyeBlockColor));
                rect(xSideOffset + getX(c), getY(r) + vertPadding, blockSize, blockSize);

                // clear active right Block
                fill(tm.bgColor);
                rect(xRightSideOffset + getX(c), getY(r) + vertPadding, blockSize, blockSize);

                // draw active left Block
                fill(tm.rightEyeBlockColor + colour);
                fill(red(colour), green(colour), blue(colour), alpha(tm.rightEyeBlockColor));
                rect(xRightSideOffset + getX(c), getY(r) + vertPadding, blockSize, blockSize);
            }
        }
    }



    /**
     * Erases the screen
     */
    public void eraseShapes() {
        // background(255, 255, 255);
        background(0, 0, 0, 0); // transparent
        // fill(tm.bgColor);
        noFill();
        stroke(tm.bgColor);
        rect(xSideOffset, vertPadding, blockSize * tm.levelwidth, blockSize * tm.levelheight);
        rect(xRightSideOffset, vertPadding, blockSize * tm.levelwidth, blockSize * tm.levelheight);
    }
    
    public void drawGridLines() {
      stroke(0, 0, 0, 10);
      for(int c = 1; c < tm.levelwidth; c++) {
        int x = map(c, 0, tm.levelwidth, width/4.0, width * 3.0/4);
        line(x, 0, x, height);
      }
      
      stroke(0, 0, 0, 20);
      line(width/2, vertPadding, width/2, vertPadding + blockSize*tm.levelheight);
      
      for(int c = 1; c < tm.levelwidth; c++) {
        int x = map(c, 0, tm.levelwidth, width/4.0, width * 3.0/4);
        line(x - 1, vertPadding, x + 1, vertPadding);
        line(x - 1, vertPadding + blockSize*tm.levelheight, x + 1, vertPadding + blockSize*tm.levelheight);
      }
    }


    /**
     * Draws the sidebar
     * Not yet added
     */
    private void drawSide() {
        textSize(21);

        // sidebar bg
        fill(178, 227, 104);
        rect(0, 0, 199, 399);

        // sidebar textbox
        fill(245, 228, 245);
        rect(15, 10, 15 + 166, 10 + 200);

        // text
        fill(0);
        text("Time: " + tm.time, 20, 30);
        text("Score: " + tm.score, 20, 59);
        text("row: " + tm.row, 20, 130);
        text("col:   " + tm.col, 20, 170);
        text("lines: " + tm.linesCleared, 20, 90);
        // text("rotation: " + tm.currentBlockRotation, 20, 190);
        // text("Block: " + tm.currentBlockType, 20, 150);

        // next Block type box
        fill(245, 218, 81);
        rect(15, 210, 15 + 166, 210 + 120);
        fill(26, 18, 26);
        text("Next: " + tm.nextBlockType, 20, 230);

        // next Block
        fill(255, 0, 0);
        rect(65, 260, 65 + 20, 260 + 20);
        rect(65, 240, 65 + 20, 240 + 20);
        if(tm.nextBlockType == Block.SQUARE) {
            rect(85, 240, 85 + 20, 240 + 20);
            rect(85, 260, 85 + 20, 260 + 20);
        }
        if(tm.nextBlockType == Block.REGULAR_L) {
            rect(65, 280, 65 + 20, 280 + 20);
            rect(85, 280, 85 + 20, 280 + 20);
        }
        if(tm.nextBlockType == Block.BACKWARDS_L) {
            rect(65, 280, 65 + 20, 280 + 20);
            rect(45, 280, 45 + 20, 280 + 20);
        }
        if(tm.nextBlockType == Block.ZIGZAG_HIGH_LEFT) {
            rect(85, 240, 85 + 20, 240 + 20);
            rect(45, 260, 45 + 20, 260 + 20);
        }
        if(tm.nextBlockType == Block.ZIGZAG_HIGH_RIGHT) {
            rect(85, 260, 85 + 20, 260 + 20);
            rect(45, 240, 45 + 20, 240 + 20);
        }
        if(tm.nextBlockType == Block.LINE) {
            rect(65, 280, 65 + 20, 280 + 20);
            rect(65, 300, 65 + 20, 300 + 20);
        }
        if(tm.nextBlockType == Block.T) {
            rect(45, 260, 45 + 20, 260 + 20);
            rect(85, 260, 85 + 20, 260 + 20);
        }

        // pause button
        if(tm.gameState == GameState.IN_GAME) {
            fill(86, 245, 96);
            rect(15, 335, 15 + 166, 335 + 25);
            fill(26, 18, 26);
            text("Pause", 66, 355);
        }
        else {
            fill(255, 0, 0);
            rect(15, 335, 15 + 166, 335 + 25);
            fill(255, 255, 255);
            text("Continue", 55, 355);
        }

        // name
        fill(26, 18, 26);
        textSize(17);
        text("By Raphael and Tristan", 72, 394);
    }

    private void drawGameOverText() {
        text("GAME OVER", xSideOffset + width/48, vertPadding + height/16);
        text("GAME OVER", xRightSideOffset + width/48, vertPadding + height/16);

        text("SCORE: " + tm.score, xSideOffset + width/48, vertPadding + height/16*2);
        text("SCORE: " + tm.score, xRightSideOffset + width/48, vertPadding + height/16*2);

        text("Lines Cleared: " + tm.linesCleared, xSideOffset + width/48, vertPadding + height/16*3);
        text("Lines Cleared: " + tm.linesCleared, xRightSideOffset + width/48, vertPadding + height/16*3);

        text("Act to restart", xSideOffset + width/48, vertPadding + height/16*4);
        text("Act to restart", xRightSideOffset + width/48, vertPadding + height/16*4);
    }

    /**
     * Draw GAME OVER on the screen
     */
    public void drawGameOverScreen() {
        // background(255, 255, 255);

        fill(0);
        textSize(blockSize);
        drawGameOverText();
        
        var outlineDisplacement = blockSize * 0.1;
        
        // outline text
        for(var x = -outlineDisplacement; x <= outlineDisplacement; x++) {
            for(var y = -outlineDisplacement; y <= outlineDisplacement; y++) {
                translate(x, y);
                drawGameOverText();
                resetMatrix();
            }
        }
        
        fill(255);
        drawGameOverText();
    }



    /**
     * Draws the main menu
     */
    public void drawMainMenu() {
        // background(157, 184, 51);
        eraseShapes();

        // "tetris"
        int tetrisR = (int) (255.0 * Math.sin(millis()/50.0) + 255);
        int tetrisG = (int) (255.0 * Math.cos(millis()/50.0) + 255);
        int tetrisB = (int) (100 + 100.0 * Math.cos(millis()/50));
        fill(tetrisR, tetrisG, tetrisB);

        fill(255);
        textSize(36);
        text("Delaze", 135.0f/400*width/2, 180.0f/400*height);
        text("Delaze", 135.0f/400*width/2 + width/2.0f, 180.0f/400*height);

        // Regular mode
        fill(255, 213, 0);
        rect(114.0f/400*width/2, 167.0f/400*height, 182/400.0f*width/2, 41/400.0f*height);
        rect(114.0f/400*width/2+width/2.0f, 167/400*height, 182400.0f*width/2+width/2.0f, 41/400.0f*height);
        textSize(24);
        fill(00, 230, 230);
        text("Click to begin", 127.0f/400*width/2, 220.0f/400*height);
        text("Click to begin", 127.0f/400*width/2+width/2.0f, 220.0f/400*height);
    }



    /**
     * Onscreen acceleration debugging:
     * Draws the x, y, and z velocities
     */
    public void drawMotion() {
        fill(255, 0, 0);
        textSize(40);

        // text("x: " + tm.motionSensor.getVelocities()[0], 79/400*width/2, height/6);
        // text("y: " + tm.motionSensor.getVelocities()[1], 79/400*width/2, height/6 + height/10);
        // text("z: " + tm.motionSensor.getVelocities()[2], 79 / 400 * width / 2, height / 6 + height / 5);
    }

    public TetrisDrawer(TetrisModel tm) {
        this.tm = tm;

        // variables reliant on screen width and height
        vertPadding = height/4;
        blockSize = (height - 2*vertPadding) / tm.levelheight;
        
        if(blockSize * tm.levelwidth * 2 > width) {
          blockSize = width/2/tm.levelwidth;
          vertPadding = (height - blockSize * tm.levelheight)/4.0;
        }
        
        xSideOffset = width/8 - blockSize*tm.levelwidth/2;
        xRightSideOffset = width - width/8 - blockSize*tm.levelwidth/2;
        
        if(xSideOffset < 0) xSideOffset = 0;
        if(xRightSideOffset > width-blockSize*tm.levelwidth) xRightSideOffset = width-blockSize*tm.levelwidth;
        
    }
}

