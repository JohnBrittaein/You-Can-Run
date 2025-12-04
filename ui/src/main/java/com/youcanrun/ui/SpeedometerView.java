package com.youcanrun.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class SpeedometerView extends View {

    private float speed = 0f; // current speed
    private float maxSpeed = 200f; // maximum speed
    private Paint dialPaint;
    private Paint needlePaint;
    private Paint textPaint;
    private RectF dialRect;

    public SpeedometerView(Context context) {
        super(context);
        init();
    }

    public SpeedometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpeedometerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(Color.GRAY);

        dialPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dialPaint.setColor(Color.DKGRAY);
        dialPaint.setStyle(Paint.Style.STROKE);
        dialPaint.setStrokeWidth(12f); // thicker for bigger look

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setColor(Color.RED);
        needlePaint.setStrokeWidth(8f); // thicker needle

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(60f); // bigger text
        textPaint.setTextAlign(Paint.Align.CENTER);

        dialRect = new RectF();
    }

    public void setSpeed(float speed) {
        this.speed = Math.max(0, Math.min(speed, maxSpeed));
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Use the full available size (or maintain square aspect)
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(width, height); // keep it square
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius = Math.min(width, height) / 2f - 40f; // extra padding for bigger look

        // Draw dial circle
        dialRect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        canvas.drawArc(dialRect, 135, 270, false, dialPaint);

        // Draw tick marks and labels
        int numTicks = 10;
        for (int i = 0; i <= numTicks; i++) {
            float angle = 135 + i * 270f / numTicks;
            double rad = Math.toRadians(angle);

            float startX = centerX + (float) (radius * 0.9 * Math.cos(rad));
            float startY = centerY + (float) (radius * 0.9 * Math.sin(rad));
            float endX = centerX + (float) (radius * Math.cos(rad));
            float endY = centerY + (float) (radius * Math.sin(rad));
            canvas.drawLine(startX, startY, endX, endY, dialPaint);

            // Speed labels
            float labelSpeed = i * maxSpeed / numTicks;
            float labelX = centerX + (float) ((radius * 0.75) * Math.cos(rad));
            float labelY = centerY + (float) ((radius * 0.75) * Math.sin(rad)) + 20; // adjust vertical offset
            canvas.drawText(String.format("%.0f", labelSpeed), labelX, labelY, textPaint);
        }

        // Draw needle
        float needleAngle = 135 + (speed / maxSpeed) * 270f;
        double needleRad = Math.toRadians(needleAngle);
        float needleX = centerX + (float) ((radius * 0.8) * Math.cos(needleRad));
        float needleY = centerY + (float) ((radius * 0.8) * Math.sin(needleRad));
        canvas.drawLine(centerX, centerY, needleX, needleY, needlePaint);

        // Center circle
        canvas.drawCircle(centerX, centerY, 18f, needlePaint); // bigger center circle
    }
}

//package com.youcanrun.ui;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.util.AttributeSet;
//import android.view.View;
//
//public class SpeedometerView extends View {
//
//    public SpeedometerView(Context context) {
//        super(context);
//        init();
//    }
//
//    public SpeedometerView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        init();
//    }
//
//    public SpeedometerView(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        init();
//    }
//
//    private void init() {
//        setBackgroundColor(Color.GRAY);
//    }
//
//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
//    }
//}
