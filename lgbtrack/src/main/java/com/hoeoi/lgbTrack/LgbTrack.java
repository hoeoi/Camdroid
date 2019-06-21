package com.hoeoi.lgbTrack;

import android.graphics.Rect;
import android.provider.Settings;
import android.util.Log;
import android.view.ViewGroup;


import java.util.concurrent.locks.ReentrantLock;

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
    private native void lgbTrackNativeProcess(byte [] data,int width ,int height);
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

    public void startTrack(){
        lgbTrackNativeInit();
        isTrackEnable = true;
        trackView.isTouchEnable = true;

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
        public ProcessImageThread(byte[] data) {
            imageData = data;
        }

        @Override
        public void run() {
            if(!isTrackEnable){
                return;
            }
            //ToDo
            lgbTrackLock.lock();
            lgbTrackNativeProcess(imageData,inputWidth,inputHeight);
            lgbTrackLock.unlock();
        }
    }

    public void processImage(final byte[] imageData) {
        ProcessImageThread thread = new ProcessImageThread(imageData);
        thread.start();
    }

    public void didLossTargetCallback(){
//        Log.d("LgbTrack","didLossTargetCallback call");
        trackView.doDrawRect(new Rect());
        if (this.callback != null){
            this.callback.didTrackControl(0,0);
        }
    }

    public void didUpdatedTargetRectCallback(int left,int top,int right,int bottom){
        Rect rect = imageRect2viewRect(new Rect(left, top, right, bottom));
        trackView.doDrawRect(rect);
        String fps = fpsMeter.measure();
//        Log.d("LgbTrack","fps:"+fps);
    }
    public void lgbTrackControlCallback(float dx,float dy){
        if (this.callback != null){
            this.callback.didTrackControl((int)dx,(int)dy);
        }
    }

    public String readUDID(){
        return Settings.Secure.getString(trackView.getContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }


}
