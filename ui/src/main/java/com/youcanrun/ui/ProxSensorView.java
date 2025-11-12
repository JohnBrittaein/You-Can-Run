package com.youcanrun.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

public class ProxSensorView extends View {

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
}
