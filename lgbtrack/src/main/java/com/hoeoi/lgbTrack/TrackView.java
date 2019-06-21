package com.hoeoi.lgbTrack;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hoeoi.lgbTrack.R;



public class TrackView extends FrameLayout {
    private float touchOriginX,touchOriginY;
    private ImageView drawView;
    private Rect toDrawRect;
    public boolean isTouchEnable;
    private TrackViewCallback callback;

    public TrackView(Context context,TrackViewCallback callback) {
        super(context);
        FrameLayout.LayoutParams fullContainParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        setLayoutParams(fullContainParams);


        Bitmap bitmap=BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.genzongkuang);

        drawView = new ImageView(context);
        drawView.setScaleType(ImageView.ScaleType.FIT_XY);
        drawView.setImageBitmap(bitmap);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                0,
                0);
        drawView.setLayoutParams(params);

        addView(drawView);

        toDrawRect = new Rect();

        this.callback = callback;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isTouchEnable){
           return super.onTouchEvent(event);
        }
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //按下
                Log.d("TrackView:","touch ACTION_DOWN");
                touchOriginX = touchX;
                touchOriginY = touchY;
                break;
            case MotionEvent.ACTION_MOVE:
                //移动
                Log.d("TrackView:","touch ACTION_MOVE");
                doDrawRect(new Rect((int) touchOriginX,(int) touchOriginY,(int) touchX,(int) touchY));
                break;
            case MotionEvent.ACTION_UP:
                //松开
                if (callback != null){
                    callback.trackViewDidSelectTarget(new Rect((int) touchOriginX, (int) touchOriginY, (int) touchX, (int) touchY));
                }
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

    public void doDrawRect(Rect rect){
        if (toDrawRect.width() != 0) {
            if (rect.width() == toDrawRect.width()
                    &&rect.height() == toDrawRect.height()
                    && Math.abs(rect.left - toDrawRect.left) < 6.0
                    && Math.abs(rect.top - toDrawRect.top) < 6.0) {
                return;
            }
        }
        toDrawRect = rect;

        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        toDrawRect.width(),
                        toDrawRect.height());
                params.topMargin = toDrawRect.top;
                params.leftMargin = toDrawRect.left;
                drawView.setLayoutParams(params);
            }
        });
    }
}
