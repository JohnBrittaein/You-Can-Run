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
    private float playerDirection;

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
    public void updateProxSensor(Vector3 monsterPosition, Vector3 playerPosition){
        float monsterDirection = POStoAngle(monsterPosition);
        playerDirection = POStoAngle(playerPosition);

        float diff = 0f;

        if ((Math.max(playerDirection,monsterDirection) - Math.min(playerDirection,monsterDirection)) > 180){
           diff = 360 - (Math.max(playerDirection,monsterDirection) - Math.min(playerDirection,monsterDirection));
        }
        else{
            diff = Math.max(playerDirection,monsterDirection) - Math.min(playerDirection,monsterDirection);
        }
        delta = ((180 - diff)/180) / 2;
        invalidate();



        Log.i("diff", "PDir: " + (playerDirection-monsterDirection));
        //Log.i("diff", "diff: " + diff);
        Log.i("diff", "delta: " + delta);
    }

    public float POStoAngle(Vector3 POS){
        float angle = 0f;
        if (POS.x < 0){//Quadrant 2 && 3
            angle = (float) (180f+(90f * (Math.atan(POS.z / POS.x)/1.57079632679)));
        }
        else if (POS.x > 0 && POS.z > 0){//Quadrant 1
            angle = (float) (90f * (Math.atan(POS.z / POS.x)/1.57079632679));
        }
        else if (POS.x > 0 && POS.z < 0){//Quadrant 4
            angle = (float) (360f+(90*(Math.atan(POS.z / POS.x)/1.57079632679)));
        }
        else if(POS.x == 1){
            angle = 0;
        }
        else if(POS.z == 1){
            angle = 90;
        }
        else if(POS.z == -1){
            angle = 270;
        }

        return angle;
    }

    public float getDelta(){
        return delta;
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
