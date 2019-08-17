package com.hoeoi.lgbTrack;

/**
 * Created by LinGB on 2019/6/4.
 */

public interface LgbTrackCallback {
     public void didTrackControl(int yaw, int pitch);
     public void didTargetLost();
}
