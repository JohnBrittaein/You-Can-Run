package com.youcanrun.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.youcanrun.utils.Vector3;

public class ProxSensorView extends View {

    Paint paint = new Paint();
    private float delta;

    public ProxSensorView(Context context) {
        super(context);
        init();
    }

    public ProxSensorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProxSensorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Color.GRAY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }
    void updateProxSensor(Vector3 monsterPosition, Vector3 playerPosition){
        float monsterDirection = (float)(180f*Math.cos(monsterPosition.z/monsterPosition.x));
        float playerDirection = (float)(180f*Math.cos(playerPosition.z/playerPosition.x));

        float diff = 0f;
        if ((playerDirection - monsterDirection) > 180){
           diff = 360 - (playerDirection - monsterDirection);
        }
        else{
            diff = playerDirection - monsterDirection;
        }

        delta = ((180 - diff)/180) / 2;
        invalidate();

        Log.i("diff", "delta: " + delta);

//        Log.i("diff", "diff: " + diff);
//        Log.i("diff", "delta: " + delta);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {

        super.onDraw(canvas);
        
        paint.setColor(Color.RED);
        paint.setStrokeWidth(50f);

//        Log.i("diff", "x: " + getWidth());
//        Log.i("diff", "y: " + getHeight());

        for (int i = 0; i < getWidth(); i++) {
            float x = (i - (getWidth() / 2f))*(10f/525f);
            float y = (float)(((184f/5f))*(Math.sin(x) + delta * (Math.sin(15 * delta * x) * Math.sin(4 * delta * x) * 40 * Math.sin((1.0/40.0) * x))) + (getHeight() / 2f));
//           float y = (float)(Math.sin(x)*(184f/5f)) + (getHeight() / 2f);
//            Log.i("diff", "x: " + x);
//            Log.i("diff", "y: " + y);
            canvas.drawCircle(i, y, 3, paint);
        }
    }
}
