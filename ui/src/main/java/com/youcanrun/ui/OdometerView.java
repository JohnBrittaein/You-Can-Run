package com.youcanrun.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
public class OdometerView extends View {
    private float distance = 0f;
    private Paint textPaint;

    public OdometerView(Context context) {
        super(context);
        init();
    }

    public OdometerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(70f);
    }

    public OdometerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setDistance(float distance) {
        this.distance = distance;
        invalidate(); // redraw the view
    }

    private void init() {
        setBackgroundColor(Color.GRAY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        String text = (distance < 1000f) ? String.format("%.2f m", distance)
                : String.format("%.2f km", distance / 1000);

        // Center text horizontally
        textPaint.setTextAlign(Paint.Align.CENTER);
        float x = getWidth() / 2f;

        // Center text vertically
        float y = getHeight() / 2f - ((textPaint.descent() + textPaint.ascent()) / 2);

        canvas.drawText(text, x, y, textPaint);
    }
}