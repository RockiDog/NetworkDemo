package com.rockidog.networkdemo;

import org.opencv.android.JavaCameraView;

import android.content.Context;

public class CameraView extends JavaCameraView {
    public CameraView(Context context, int cameraID) {
        super(context, cameraID);
        setZOrderOnTop(true);
    }
}
