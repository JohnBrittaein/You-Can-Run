package com.youcanrun.ui;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

public class HudStyle {
    public static final int PRIMARY_COLOR = Color.GREEN;        // neon green
    public static final int SECONDARY_COLOR = Color.CYAN;       // accents
    public static final int WARNING_COLOR = Color.RED;

    public static Paint getPaint(float strokeWidth, boolean isFilled){
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(PRIMARY_COLOR);
        p.setStrokeWidth(strokeWidth);
        p.setStyle(isFilled ? Paint.Style.FILL : Paint.Style.STROKE);
        p.setTypeface(Typeface.MONOSPACE);
        return p;
    }
}

