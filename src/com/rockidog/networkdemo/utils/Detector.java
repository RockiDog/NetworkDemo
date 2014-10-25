package com.rockidog.networkdemo.utils;

import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

public class Detector implements CvCameraViewListener2 {
    private static final String TAG = "Detector";
    private Scalar mRectColor = new Scalar(255, 0, 0, 255);
    private int mRectThickness = 3;

    private long mCascadeClassifier;
    private Mat mRgbaFrame;
    private Mat mGrayFrame;

    public Detector(String cascadePath) { mCascadeClassifier = nativeCreate(cascadePath); }
    public void Destroy() { nativeDestroy(mCascadeClassifier); }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgbaFrame = new Mat();
        mGrayFrame = new Mat();
        nativeStart(mCascadeClassifier);
    }

    @Override
    public void onCameraViewStopped() {
        nativeStop(mCascadeClassifier);
        mRgbaFrame.release();
        mGrayFrame.release();
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgbaFrame = inputFrame.rgba();
        mGrayFrame = inputFrame.gray();
        MatOfRect faces = new MatOfRect();
        nativeDetect(mCascadeClassifier, mGrayFrame.getNativeObjAddr(), faces.getNativeObjAddr());
        Rect[] rectArrary = faces.toArray();
        for (int i = 0; rectArrary.length != i; ++i)
            Core.rectangle(mRgbaFrame, rectArrary[i].tl(), rectArrary[i].br(), mRectColor, mRectThickness);
        return mRgbaFrame;
    }

    private native long nativeCreate(String cascadePath);
    private native void nativeStart(long cascadeClassifier);
    private native void nativeDetect(long cascadeClassifier, long inputFrame, long faces);
    private native void nativeStop(long cascadeClassifier);
    private native void nativeDestroy(long cascadeClassifier);
}
