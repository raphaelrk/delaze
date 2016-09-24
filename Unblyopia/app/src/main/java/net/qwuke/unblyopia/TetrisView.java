package net.qwuke.unblyopia;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Vibrator;
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

    final TetrisModel tm;
    private final TetrisDrawer td;

    private final Boolean isHeadTrackingEnabled;

    /**
     * Main loop
     * Gets called every cycle
     * Updates things and draws them
     *
     * @param canvas, required for drawing to the screen
     */
    protected void onDraw(Canvas canvas) {
        td.setCanvas(canvas);
        if(tm.gameState == TetrisModel.GameState.IN_GAME) {

            td.eraseShapes();
            td.drawShapes();
            //drawSide();
            td.drawHUD();

            if(isHeadTrackingEnabled) {
                tm.setInitAngle();
                tm.motionSensorMove();
            }

            if(tm.bottomCollision()) {
                tm.removeLines();
            }

            if(tm.time % tm.updateSpeed == 0) {
                tm.moveDown();
            }

            tm.time++;
        } else if(tm.gameState == TetrisModel.GameState.GAME_OVER) {
            td.drawGameOverScreen();
        } else if(tm.gameState == TetrisModel.GameState.PAUSED) {

        } else if(tm.gameState == TetrisModel.GameState.MAIN_MENU) {
            td.drawMainMenu();
        }

        // td.drawMotion();
        //Log.d(MainActivity.TAG, "Vx: " + tm.motionSensor.getVelocities()[0]);
        //Log.d(MainActivity.TAG, "Vy: " + tm.motionSensor.getVelocities()[1]);
        //Log.d(MainActivity.TAG, "Vz: " + tm.motionSensor.getVelocities()[2]);

        // force redraw
        super.postInvalidate();
    }

    /**
     * Constructor
     * Instantiates variables
     *
     * @param context Activity, required of all views
     * @param motionSensorModule accelerometer
     * @param vibrator for vibrating
     * @param headTracking whether head tracking is enabled
     */
    public TetrisView(Context context, MotionSensorModule motionSensorModule, Vibrator vibrator, Boolean headTracking, int[] globalColours, int interLensOffset) {
        super(context);

        isHeadTrackingEnabled = headTracking;

        tm = new TetrisModel(motionSensorModule, vibrator, globalColours);

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        td = new TetrisDrawer(wm, tm, interLensOffset);
    }
}
