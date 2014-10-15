package com.rockidog.networkdemo.utils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;

import android.content.Context;

import java.io.File;

public class Detector implements CvCameraViewListener2 {
    private BaseLoaderCallback mCallback = null;
    private File mCascadeFile = null;

    public Detector(Context appContext, String cascadeDir, String cascadeName) {
        mCascadeFile = new File(cascadeDir, cascadeName);
        mCallback = new BaseLoaderCallback(appContext) {
            @Override
            public void onManagerConnected(int status) {
                switch (status) {
                    case LoaderCallbackInterface.SUCCESS:
                        System.loadLibrary("CCvDetector");
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, appContext, mCallback);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat rgbaFrame = inputFrame.rgba();
        Mat grayFrame = inputFrame.gray();
        return rgbaFrame;
    }

    private native void nativeCreate();
    private native void nativeStart();
    private native void nativeDetect();
    private native void nativeStop();
    private native void nativeDestroy();
}
