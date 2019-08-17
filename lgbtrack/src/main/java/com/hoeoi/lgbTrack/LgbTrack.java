package com.hoeoi.lgbTrack;

import android.graphics.Rect;
import android.provider.Settings;
import android.util.Log;
import android.view.ViewGroup;


import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Handler;

public class LgbTrack implements TrackViewCallback{
    private TrackView trackView;
    public boolean isTrackEnable;
    private Rect trackTargetRect;
    private LgbTrackCallback callback;
    private ReentrantLock lgbTrackLock;
    private int inputWidth,inputHeight; //输入图像尺寸
    private int scaleWidth,scaleHeight; //缩小之后图像尺寸
    private final double image_scale = 0.25;
    FpsMeter fpsMeter;
    private Timer timer;
    private TimerTask task;
    private int timeoutCnt = 0;
    public int timeout = 8;

    public LgbTrack(ViewGroup view, int imageWidth, int imageHeight) {
        trackView = new TrackView(view.getContext(),this);
        view.addView(trackView);
        lgbTrackLock = new ReentrantLock();
        inputWidth = imageWidth;
        inputHeight = imageHeight;
        scaleWidth = (int)(inputWidth * image_scale);
        scaleHeight = (int)(inputHeight * image_scale);
        fpsMeter = new FpsMeter(0,0);



    }

    public void setCallback(LgbTrackCallback callback){
        this.callback = callback;
    }

    private native void lgbTrackNativeInit();
    private native void lgbTrackNativeDeinit();
    private native void lgbTrackNativeSetTrackTarget(int x,int y,int width,int height);
    private native void lgbTrackNativeProcess(byte [] data,int width ,int height,int orientation);
    static {
        System.loadLibrary("lgbTrack");
    }
    @Override
    public void trackViewDidSelectTarget(Rect rect) {
//        Log.d("lgbTrack","did selected rect:"+rect.toString());
        trackView.isTouchEnable = false;
        Rect imageTarget = viewRect2imageRect(rect);
        lgbTrackLock.lock();
        lgbTrackNativeSetTrackTarget(imageTarget.left,imageTarget.top,imageTarget.width(),imageTarget.height());
        lgbTrackLock.unlock();
    }
    private void startTimeoutCheck(){
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                timeoutCnt++;
                if (timeoutCnt == timeout) {
                    timeoutCnt = 0;
                    //
//                    Log.e("lgbTrack","丢失目标找回超时");
                    callback.didTargetLost();

                }
            }
        };
        timeoutCnt = 0;
        timer.schedule(task,0,1000);
    }
    private void stopTimeoutCheck(){
        timer.cancel();
    }
    public void startTrack(){
        lgbTrackNativeInit();
        isTrackEnable = true;
        trackView.isTouchEnable = true;
        startTimeoutCheck();
    }

    public void stopTrack(){
        lgbTrackLock.lock();
        isTrackEnable = false;
        trackView.isTouchEnable = false;
        lgbTrackNativeDeinit();
        trackTargetRect = new Rect();
        trackView.doDrawRect(new Rect());
        if (this.callback != null){
            this.callback.didTrackControl(0,0);
        }
        lgbTrackLock.unlock();
        stopTimeoutCheck();
    }

    private Rect viewRect2imageRect(Rect rect){


        float widthK = (float) scaleWidth / trackView.getWidth();
        float heightK = (float) scaleHeight / trackView.getHeight();

        Rect retRect = new Rect();
        retRect.left = (int)(rect.left * widthK);
        retRect.top = (int)(rect.top * heightK);
        retRect.right = (int)(rect.right *widthK);
        retRect.bottom = (int)(rect.bottom * heightK);

        return retRect;

    }

    private Rect imageRect2viewRect(Rect rect){
        float widthK = (float)trackView.getWidth()/ scaleWidth;
        float heightK = (float)trackView.getHeight()/ scaleHeight;

        Rect retRect = new Rect();
        retRect.left = (int)(rect.left * widthK);
        retRect.top = (int)(rect.top * heightK);
        retRect.right = (int)(rect.right *widthK);
        retRect.bottom = (int)(rect.bottom * heightK);

        return retRect;
    }
    private class ProcessImageThread extends Thread{
        private byte[] imageData;
        private int orientation;
        public ProcessImageThread(byte[] data,int orientation) {
            imageData = data;
            this.orientation = orientation;
        }

        @Override
        public void run() {
            if(!isTrackEnable){
                return;
            }
            //ToDo
            lgbTrackLock.lock();
            lgbTrackNativeProcess(imageData,inputWidth,inputHeight,orientation);
            lgbTrackLock.unlock();
        }
    }

    public void processImage(final byte[] imageData,int orientation) {
        ProcessImageThread thread = new ProcessImageThread(imageData,orientation);
        thread.start();
    }

    public void didLossTargetCallback(){
//        Log.d("LgbTrack","didLossTargetCallback call");
        trackView.doDrawRect(new Rect());
//        if (this.callback != null){
//            this.callback.didTrackControl(0,0);
//        }
    }

    public void didUpdatedTargetRectCallback(int left,int top,int right,int bottom){
        Rect rect = imageRect2viewRect(new Rect(left, top, right, bottom));
        trackView.doDrawRect(rect);
        String fps = fpsMeter.measure();
        Log.d("LgbTrack","fps:"+fps);
    }
    public void lgbTrackControlCallback(float dx,float dy){
        if (this.callback != null){
            this.callback.didTrackControl((int)dx,(int)dy);
        }
        timeoutCnt = 0;
    }

    public String readUDID(){
        return Settings.Secure.getString(trackView.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }


}
