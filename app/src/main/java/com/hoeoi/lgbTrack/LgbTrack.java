package com.hoeoi.lgbTrack;

import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.hschott.camdroid.OnCameraPreviewListener;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

public class LgbTrack implements TrackViewCallback,OnCameraPreviewListener{
    private TrackView trackView;
    public boolean isTrackEnable;
    private Rect trackTargetRect;
    private LgbTrackCallback callback;
    private ReentrantLock lgbTrackLock;
    private int imageWidth,imageHeight;
    private final double image_scale;
    public LgbTrack(ViewGroup view, int imageWidth, int imageHeight) {
        image_scale = 0.25;
        trackView = new TrackView(view.getContext(),this);
        view.addView(trackView);
        lgbTrackLock = new ReentrantLock();
        this.imageWidth = (int)(imageWidth * image_scale);
        this.imageHeight = (int)(imageHeight * image_scale);
        startTrack();
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
        Log.d("lgbTrack","did selected rect:"+rect.toString());
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


        float widthK = (float) imageWidth / trackView.getWidth();
        float heightK = (float) imageHeight / trackView.getHeight();

        Rect retRect = new Rect();
        retRect.left = (int)(rect.left * widthK);
        retRect.top = (int)(rect.top * heightK);
        retRect.right = (int)(rect.right *widthK);
        retRect.bottom = (int)(rect.bottom * heightK);

        return retRect;


    }

    private Rect imageRect2viewRect(Rect rect){
        float widthK = (float)trackView.getWidth()/ imageWidth;
        float heightK = (float)trackView.getHeight()/ imageHeight;

        Rect retRect = new Rect();
        retRect.left = (int)(rect.left * widthK);
        retRect.top = (int)(rect.top * heightK);
        retRect.right = (int)(rect.right *widthK);
        retRect.bottom = (int)(rect.bottom * heightK);

        return retRect;
    }

    public void processImage(byte[] imageData){
        if(!isTrackEnable){
            return;
        }
        //ToDo
        lgbTrackLock.lock();
        lgbTrackNativeProcess(imageData,1280,720);
        lgbTrackLock.unlock();
    }

    public void didLossTargetCallback(){
        Log.d("LgbTrack","didLossTargetCallback call");
    }

    public void didUpdatedTargetRectCallback(int left,int top,int right,int bottom){
        trackView.doDrawRect(new Rect(left,top,right,bottom));
    }

    public void lgbTrackControlCallback(float dx,float dy){
        if (this.callback != null){
            this.callback.didTrackControl((int)dx,(int)dy);
        }
    }

    @Override
    public void onCameraPreviewFrame(byte[] data, int previewFormat) {
        this.processImage(data);
    }

    @Override
    public void onCameraPreviewStarted(Camera camera) {

    }

    @Override
    public void onCameraPreviewStopped() {

    }
}
