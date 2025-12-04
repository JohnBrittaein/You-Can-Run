package com.youcanrun.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class SignalStrengthView extends View {

    private static final int MAX_STRENGTH = 10; // number of bars
    private int signalStrength = 0; // 0 to MAX_STRENGTH

    private Paint barPaint;
    private int[] barColors;

    private Drawable baseDrawable;
    private Drawable glowDrawable;
    private Drawable rippleDrawable;
    private Drawable selectorDrawable;

    public SignalStrengthView(Context context) {
        super(context);
        init(context);
    }

    public SignalStrengthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SignalStrengthView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // Initialize bar paint
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setStyle(Paint.Style.FILL);

        // Gradient colors from green -> yellow -> red
        barColors = new int[]{
                Color.GREEN,
                Color.GREEN,
                Color.YELLOW,
                Color.rgb(255, 165, 0), // orange
                Color.RED
        };

        // Load custom drawables
        baseDrawable = ContextCompat.getDrawable(context, R.drawable.button_base);
        glowDrawable = ContextCompat.getDrawable(context, R.drawable.button_glow);
        rippleDrawable = ContextCompat.getDrawable(context, R.drawable.button_ripple);
        selectorDrawable = ContextCompat.getDrawable(context, R.drawable.button_selector);
    }

    public void setSignalStrength(int strength) {
        if (strength < 0) strength = 0;
        if (strength > MAX_STRENGTH) strength = MAX_STRENGTH;
        signalStrength = strength;
        invalidate(); // redraw
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        int barSpacing = 4; // vertical gap between bars
        int singleBarHeight = (height - (MAX_STRENGTH - 1) * barSpacing) / MAX_STRENGTH;

        // Draw bars from bottom to top
        for (int i = 0; i < MAX_STRENGTH; i++) {
            int bottom = height - i * (singleBarHeight + barSpacing);
            int top = bottom - singleBarHeight;
            int left = 0;
            int right = width;

            // Draw base drawable for this bar
            if (baseDrawable != null) {
                baseDrawable.setBounds(left, top, right, bottom);
                baseDrawable.draw(canvas);
            }

            // Draw the bar color based on signal strength
            if (i < signalStrength) {
                barPaint.setColor(barColors[Math.min(i, barColors.length - 1)]);
            } else {
                barPaint.setColor(Color.LTGRAY);
            }
            canvas.drawRect(left, top, right, bottom, barPaint);

            // Optional: draw glow or selector on top of each bar
            if (glowDrawable != null) {
                glowDrawable.setBounds(left, top, right, bottom);
                glowDrawable.draw(canvas);
            }
            if (selectorDrawable != null) {
                selectorDrawable.setBounds(left, top, right, bottom);
                selectorDrawable.draw(canvas);
            }
        }
    }
}
