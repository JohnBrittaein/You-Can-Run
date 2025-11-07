package com.youcanrun.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.youcanrun.utils.Vector3;

public class DevMapView extends View {

    private Paint paintMonster = new Paint();
    private Paint paintPlayer = new Paint();

    private Vector3 monsterPosition = new Vector3(0, 0, 0);
    private Vector3 playerDirection = new Vector3(0, 0, 1); // default facing +Z
    private float mapScale = 2f;

    public DevMapView(Context context) {
        super(context);
        init();
    }

    public DevMapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DevMapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        paintMonster.setColor(Color.RED);
        paintMonster.setStyle(Paint.Style.FILL);

        paintPlayer.setColor(Color.GREEN);
        paintPlayer.setStyle(Paint.Style.STROKE);
        paintPlayer.setStrokeWidth(4f);
    }

    public void setMonsterPosition(Vector3 pos) {
        if (pos != null) {
            this.monsterPosition = pos;
            invalidate();
        }
    }

    public void setPlayerDirection(Vector3 dir) {
        if (dir != null) {
            this.playerDirection = dir.normalize(); // make unit vector
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Clear map with semi-transparent background
        canvas.drawColor(Color.argb(100, 0, 0, 0));

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        // Draw monster relative to player
        float monsterX = cx + monsterPosition.x * mapScale;
        float monsterY = cy - monsterPosition.z * mapScale;
        canvas.drawCircle(monsterX, monsterY, 5, paintMonster);

        // Draw player at center
        canvas.drawCircle(cx, cy, 10, paintPlayer);

        // Draw player facing direction as an arrow
        float arrowLength = 30f;
        float arrowX = cx + playerDirection.x * arrowLength;
        float arrowY = cy - playerDirection.z * arrowLength;
        canvas.drawLine(cx, cy, arrowX, arrowY, paintPlayer);
    }
}
