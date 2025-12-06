package com.youcanrun.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class SignalSliderView extends View {

    private Paint fillPaint;
    private float fillFraction = 0.5f; // 0 = empty, 1 = full
    private Drawable backgroundDrawable;

    public SignalSliderView(Context context) {
        super(context);
        init(context);
    }

    public SignalSliderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SignalSliderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // Load your custom ripple drawable as background
        backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.slider);
        setClickable(true);

        // Paint for the green fill
        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(0xFF00FF00); // green
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        // Draw custom ripple background
        if (backgroundDrawable != null) {
            backgroundDrawable.setBounds(0, 0, width, height);
            backgroundDrawable.draw(canvas);
        }

        // Draw green filled portion
        float filledHeight = fillFraction * height;
        canvas.drawRect(0, height - filledHeight, width, height, fillPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                // Convert y to fillFraction (0 at bottom, 1 at top)
                fillFraction = 1f - Math.max(0f, Math.min(y / getHeight(), 1f));
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    public float getFillFraction() {
        return fillFraction;
    }

    public void setFillFraction(float fraction) {
        fillFraction = Math.max(0f, Math.min(fraction, 1f));
        invalidate();
    }
}
