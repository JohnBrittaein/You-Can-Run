package com.youcanrun.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

import com.youcanrun.utils.Vector3;

public class CompassView extends View {
    private Paint paintPlayer = new Paint();
    private Vector3 playerDirection = new Vector3(0, 0, 1); // default facing +Z

    private Paint circlePaint;

    public CompassView(Context context) {
        super(context);
        init();
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CompassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setCompassDirection(Vector3 delta) {
        if (delta != null) {
            this.playerDirection = delta.normalize(); // make unit vector
            invalidate();
        }
    }

    private void init() {
        setBackgroundColor(Color.GRAY);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        //set the player color
        paintPlayer.setColor(Color.argb(255, 0, 255, 170));
        //set the canvas color
        canvas.drawColor(Color.argb(100, 0, 0, 0));

        paintPlayer.setStrokeWidth(10f); // Set the desired thickness in pixels


        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        // Draw player at center
        canvas.drawCircle(cx, cy, 20, paintPlayer);


        // Draw player facing direction as an arrow
        float arrowLength = 50f;
        float arrowX = cx + playerDirection.x * arrowLength;
        float arrowY = cy - playerDirection.z * arrowLength;
        canvas.drawLine(cx, cy, arrowX, arrowY, paintPlayer);
    }
}
