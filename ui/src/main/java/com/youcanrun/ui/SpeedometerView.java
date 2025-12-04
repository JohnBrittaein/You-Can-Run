package com.youcanrun.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class SpeedometerView extends View {

    private float speed = 0f; // current speed
    private Paint textPaint;

    public SpeedometerView(Context context) {
        super(context);
        init();
    }

    public SpeedometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(70f);
    }

    public SpeedometerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        invalidate();
    }

    private void init() {
        setBackgroundColor(Color.GRAY);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        String text = String.format("%.2f m/s", speed);

        // Center text horizontally
        textPaint.setTextAlign(Paint.Align.CENTER);
        float x = getWidth() / 2f;

        // Center text vertically
        float y = getHeight() / 2f - ((textPaint.descent() + textPaint.ascent()) / 2);

        canvas.drawText(text, x, y, textPaint);
    }
}
