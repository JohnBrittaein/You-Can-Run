package com.youcanrun.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public abstract class HudView extends View {
    public HudView(Context context){
        super(context);
    }
    public HudView(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        drawHud(canvas);
    }

    protected abstract void drawHud(Canvas canvas);
}
