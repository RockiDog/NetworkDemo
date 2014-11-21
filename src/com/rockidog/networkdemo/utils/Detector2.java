package com.rockidog.networkdemo.utils;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;

public class Detector2 {
    private long mNativeClassifier = 0;

    public Detector2(String cascadeName) { mNativeClassifier = nativeCreate(cascadeName); }
    public void Start() { nativeStart(mNativeClassifier); }
    public void Stop() { nativeStop(mNativeClassifier); }
    public void Detect(Mat imageGray, MatOfRect faces) { nativeDetect(mNativeClassifier, imageGray.getNativeObjAddr(), faces.getNativeObjAddr()); }
    public void Release() { nativeDestroy(mNativeClassifier); mNativeClassifier = 0; }

    private native long nativeCreate(String cascadeName);
    private native void nativeDestroy(long classifier);
    private native void nativeStart(long classifier);
    private native void nativeStop(long classifier);
    private native void nativeDetect(long classifier, long inputFrame, long faces);
}
