package com.hoeoi.lgbTrack;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class TrackView extends FrameLayout {
    private float touchOriginX,touchOriginY;
    private float drawRectWidth,drawRectHeight;
    private View drawView;
    public TrackView(Context context) {
        super(context);
        FrameLayout.LayoutParams fullContainParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        setLayoutParams(fullContainParams);

        drawView = new View(context);
        drawView.setBackgroundColor(Color.BLUE);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                0,
                0);
        drawView.setLayoutParams(params);

        addView(drawView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                //按下
                Log.d("TrackView:","touch ACTION_DOWN");
                touchOriginX = event.getX();
                touchOriginY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //移动
                Log.d("TrackView:","touch ACTION_MOVE");
                float touchX = event.getX();
                float touchY = event.getY();
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        (int) (touchX-touchOriginX),
                        (int) (touchY-touchOriginY));
                params.topMargin = (int) touchOriginY;
                params.leftMargin = (int) touchOriginX;
                drawView.setLayoutParams(params);
                break;
            case MotionEvent.ACTION_UP:
                //松开
                Log.d("TrackView:","touch ACTION_UP");
                break;
        }
        //super.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }
}
