package net.qwuke.unblyopia;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Raphael on 9/6/2014.
 */
public class TetrisView extends View {
    private ShapeDrawable mDrawable;
    Paint paint = new Paint();

    public TetrisView(Context context) {
        super(context);}

    protected void onDraw(Canvas canvas) {
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

        /*mDrawable = new ShapeDrawable(new RectShape());
        mDrawable.getPaint().setColor(0x111111);
        mDrawable.setBounds(x, y, x + width, y + height);


        mDrawable.draw(canvas);*/
    }
}
