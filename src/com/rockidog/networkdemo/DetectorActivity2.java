package com.rockidog.networkdemo;

import com.rockidog.networkdemo.utils.Detector2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;
import android.util.Log;

public class DetectorActivity2 extends Activity implements CvCameraViewListener2 {
    private static final String TAG = "Detector2";
    private static final Scalar mRectColor = new Scalar(0, 255, 0, 255);
    private static final int mRectThickness = 3;

    private Mat mRgbaFrame;
    private Mat mGrayFrame;
    private File mCascadeFile;
    private Detector2 mNativeDetector;
    private JavaCameraView mCameraView;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            Log.i(TAG, "Inside onManagerConnected");
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i(TAG, "Callback called successfully");
                    Log.i(TAG, "Loading libCvDetector2.so...");
                    System.loadLibrary("CvDetector2");
                    try {
                        InputStream is = getResources().openRawResource(R.raw.haarcascade_profileface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "classifier.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while (-1 != (bytesRead = is.read(buffer)))
                            os.write(buffer, 0, bytesRead);
                        is.close();
                        os.close();
                        mNativeDetector = new Detector2(mCascadeFile.getAbsolutePath());
                        cascadeDir.delete();
                    } catch (IOException e) {
                        Log.e(TAG, "Cascade file exception");
                        e.printStackTrace();
                    }
                    Log.i(TAG, "Enable camera view");
                    mCameraView.enableView();
                    break;
                default:
                    Log.e(TAG, "Fail to call the callback");
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Inside onCreate");
        super.onCreate(savedInstanceState);
        mCameraView = new JavaCameraView(this, JavaCameraView.CAMERA_ID_BACK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(mCameraView);
        Log.i(TAG, "Trying to set camera view listener");
        mCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        Log.i(TAG, "Inside onPause");
        super.onPause();
        if (mCameraView != null) {
            Log.i(TAG, "Disable camera view");
            mCameraView.disableView();
        }
    }

    @Override
    public void onResume() {
        Log.i(TAG, "Inside onResume");
        super.onResume();
        Log.i(TAG, "Trying to initialize OpenCV");
        if (false == OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mLoaderCallback))
            Log.e(TAG, "Fail to initialize OpenCV");
        else
            Log.i(TAG, "OpenCV initialized");
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Inside onDestroy");
        super.onDestroy();
        if (mCameraView != null)
            mCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mGrayFrame = new Mat();
        mRgbaFrame = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mGrayFrame.release();
        mRgbaFrame.release();
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgbaFrame = inputFrame.rgba();
        mGrayFrame = inputFrame.gray();
        MatOfRect faces = new MatOfRect();
        if (null != mNativeDetector)
            mNativeDetector.Detect(mGrayFrame, faces);
        Rect[] facesArray = faces.toArray();
        Log.w(TAG, Integer.toString(facesArray.length));
        for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(mRgbaFrame, facesArray[i].tl(), facesArray[i].br(), mRectColor, mRectThickness);
        return mRgbaFrame;
    }
}
