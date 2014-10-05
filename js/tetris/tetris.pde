/**
 * Tetris
 * By Raphael Rouvinov-Kats
 * 
 * Tetris implementation for Processing / processing.js
 * Embedded in http://www.delaze.me
 */
 
/* @pjs transparent="true"; */

TetrisModel tm;
TetrisDrawer td;

public void setup() {
  size(window.innerWidth, window.innerHeight);
  frameRate(frame_rate);
  tm = new TetrisModel();
  td = new TetrisDrawer(tm);
  background(0, 0, 0, 0);
}

/**
 * Main loop
 * Gets called every cycle
 * Updates things and draws them
 *
 * @param canvas, required for drawing to the screen
 */
void draw() {
    td = new TetrisDrawer(tm); // handling screen resizes
    
    if(tm.gameState == GameState.IN_GAME) {
        td.eraseShapes();
        td.drawGridLines();
        td.drawShapes();
        //drawSide();

        tm.motionSensorMove();

        if(tm.bottomCollision()) {
            tm.removeLines();
        }

        if(tm.time % tm.updateSpeed == 0) {
            tm.moveDown();
        }

        tm.time++;
    } else if(tm.gameState == GameState.GAME_OVER) {
        td.drawGameOverScreen();
    } else if(tm.gameState == GameState.PAUSED) {

    } else if(tm.gameState == GameState.MAIN_MENU) {
        td.drawMainMenu();
    }
}

void keyPressed() {
  if(key == LEFT) tm.keyPressed(Key.LEFT);
  if(key == RIGHT) tm.keyPressed(Key.RIGHT);
  if(key == UP) tm.keyPressed(Key.UP);
  if(key == DOWN) tm.keyPressed(Key.DOWN);
}

void mouseReleased() {
  tm.actionButton();
}
