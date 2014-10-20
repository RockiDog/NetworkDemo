package com.rockidog.networkdemo.utils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import android.content.Context;

public class Detector implements CvCameraViewListener2 {
    private BaseLoaderCallback mCallback;
    private long mCascadeClassifier;
    private Scalar mRectColor = new Scalar(255, 0, 0, 255);
    private int mRectThickness = 3;

    public Detector(Context appContext, String cascadePath) {
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
        mCascadeClassifier = nativeCreate(cascadePath);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {}

    @Override
    public void onCameraViewStopped() {}

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat rgbaFrame = inputFrame.rgba();
        Mat grayFrame = inputFrame.gray();
        MatOfRect faces = new MatOfRect();
        nativeDetect(mCascadeClassifier, grayFrame.getNativeObjAddr(), faces.getNativeObjAddr());
        Rect[] rectArrary = faces.toArray();
        for (int i = 0; rectArrary.length != i; ++i) {
            Core.rectangle(rgbaFrame, rectArrary[i].tl(), rectArrary[i].br(), mRectColor, mRectThickness);
        }
        return rgbaFrame;
    }

    private native long nativeCreate(String cascadePath);
    private native void nativeStart(long cascadeClassifier);
    private native void nativeDetect(long cascadeClassifier, long inputFrame, long faces);
    private native void nativeStop(long cascadeClassifier);
    private native void nativeDestroy(long cascadeClassifier);
}
