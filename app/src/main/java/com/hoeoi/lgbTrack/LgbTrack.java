package com.hoeoi.lgbTrack;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class LgbTrack {
    private TrackView trackView;
    public LgbTrack(ViewGroup view) {
        trackView = new TrackView(view.getContext());

        view.addView(trackView);

    }
}
