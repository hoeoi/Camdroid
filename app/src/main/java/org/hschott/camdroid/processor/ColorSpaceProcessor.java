package org.hschott.camdroid.processor;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import org.hschott.camdroid.ConfigurationFragment;
import org.hschott.camdroid.OnCameraPreviewListener.FrameDrawer;
import org.hschott.camdroid.R;

public class ColorSpaceProcessor extends AbstractOpenCVFrameProcessor {

    private static int channel = R.id.hsv;

    public static class ColorSpaceUIFragment extends ConfigurationFragment {

        @Override
        public int getLayoutId() {
            return R.layout.colorspace_ui;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = super
                    .onCreateView(inflater, container, savedInstanceState);

            RadioGroup colorspace = (RadioGroup) v.findViewById(R.id.colorspace);
            colorspace.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    channel = checkedId;
                }
            });

            ((RadioButton) v.findViewById(channel)).setChecked(true);

            return v;
        }
    }

    @Override
    public Fragment getConfigUiFragment(Context context) {
        return Fragment.instantiate(context, ColorSpaceUIFragment.class.getName());
    }

    public ColorSpaceProcessor(FrameDrawer drawer) {
        super(drawer);
    }

    @Override
    public FrameWorker createFrameWorker() {
        return new ColorSpaceFrameWorker(drawer);
    }

    public class ColorSpaceFrameWorker extends AbstractOpenCVFrameWorker {
        public ColorSpaceFrameWorker(FrameDrawer drawer) {
            super(drawer);
        }

        protected void execute() {
            out = rgb();
        }

    }


}