package com.rockidog.networkdemo;

import com.rockidog.networkdemo.utils.Detector2;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.WindowManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class DetectorActivity2 extends Activity implements Runnable, CvCameraViewListener2 {
    private static final int TIME = 30;
    private static final String TAG = "Detector2";
    private static final Scalar mRectColor = new Scalar(0, 255, 0, 255);
    private static final int mRectThickness = 3;
    private static Rect mMaxFace;

    private DatagramSocket mDatagramSocket;
    private InetAddress mBroadcastAddress;
    private int mPort = 7001;
    private byte[] mBuffer;
    private Thread mSocketThread;
    private boolean isLost;
    private boolean isPaused = false;

    private Mat mRgbaFrame;
    private Mat mGrayFrame;
    private File mCascadeFile;
    private Detector2 mFrontalDetector;
    private Detector2 mProfileDetector;
    private JavaCameraView mCameraView;

    private Point mThisPoint;
    private Point mCenter;

    public static String mIP;

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
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "classifier.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while (-1 != (bytesRead = is.read(buffer)))
                            os.write(buffer, 0, bytesRead);
                        is.close();
                        os.close();
                        mFrontalDetector = new Detector2(mCascadeFile.getAbsolutePath());
                        cascadeDir.delete();
                        
                        is = getResources().openRawResource(R.raw.lbpcascade_profileface);
                        cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "classifier2.xml");
                        os = new FileOutputStream(mCascadeFile);
                        while (-1 != (bytesRead = is.read(buffer)))
                            os.write(buffer, 0, bytesRead);
                        is.close();
                        os.close();
                        mProfileDetector = new Detector2(mCascadeFile.getAbsolutePath());
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
        mCameraView = new JavaCameraView(this, JavaCameraView.CAMERA_ID_FRONT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.i(TAG, "Trying to set camera view listener");
        mCameraView.setCvCameraViewListener(this);
        mCameraView.enableFpsMeter();
        setContentView(mCameraView);
        mCenter = new Point(640, 360);
        mSocketThread = new Thread(this);
        mSocketThread.start();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "Inside onPause");
        super.onPause();
        if (null != mCameraView) {
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
        if (null != mCameraView)
            mCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.i(TAG, "onCameraViewStarted");
        mGrayFrame = new Mat();
        mRgbaFrame = new Mat();
        isPaused = false;
    }

    @Override
    public void onCameraViewStopped() {
        Log.i(TAG, "onCameraViewStopped");
        mGrayFrame.release();
        mRgbaFrame.release();
        isPaused = true;
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Core.flip(inputFrame.rgba(), mRgbaFrame, 1);
        Core.flip(inputFrame.gray(), mGrayFrame, 1);
        MatOfRect frontal = new MatOfRect();
        MatOfRect profile = new MatOfRect();
        
        if (null != mFrontalDetector)
            mFrontalDetector.Detect(mGrayFrame, frontal);
        if (null != mProfileDetector)
            mProfileDetector.Detect(mGrayFrame, profile);
        
        ArrayList<Rect> facesArray = new ArrayList<Rect>(frontal.toList());
        facesArray.addAll(profile.toList());
        if (0 != facesArray.size()) {
            isLost = false;
            mMaxFace = facesArray.get(0);
            for (Rect face: facesArray)
                if (mMaxFace.width * mMaxFace.height < face.width * face.height)
                    mMaxFace = face;
        }
        else {
            isLost = true;
        }
        
        if (null != mMaxFace) {
            mThisPoint = new Point((mMaxFace.br().x + mMaxFace.tl().x) / 2, (mMaxFace.br().y + mMaxFace.tl().y) / 2);
            Core.rectangle(mRgbaFrame, mMaxFace.tl(), mMaxFace.br(), mRectColor, mRectThickness);
            Core.putText(mRgbaFrame, mThisPoint.toString(), mThisPoint, Core.FONT_HERSHEY_PLAIN, 2, mRectColor, mRectThickness);
        }
        return mRgbaFrame;
    }

    @Override
    public void run() {
        try {
            mBroadcastAddress = InetAddress.getByName(mIP);
            mDatagramSocket = new DatagramSocket();
        }
        catch (SocketException s) {
            s.printStackTrace();
            mDatagramSocket.close();
        }
        catch (UnknownHostException u) {
            u.printStackTrace();
            mDatagramSocket.close();
        }
        
        while (false == isPaused) {
            long startTime = SystemClock.uptimeMillis();
            
            try {
                // L: Left joystick
                // R: Right joystick
                // D: Direction in angle (by 0.01 degree, [0, 36000])
                // S: Speed of vehicle in percent ([0, 100])
                // B: Button (0: shooting button, 1: dribbling button, 2: invalid button)
                // P: Power in percent ([0, 100])
                String buffer;
                if (true == isLost || null == mThisPoint) {
                    buffer = "LD0S0";
                }
                else {
                    if (mThisPoint.x < mCenter.x)  // Move right
                        buffer = "LD0S20";
                    else                           // Move left
                        buffer = "LD18000S20";
                }
                buffer += "RD0S0B2P0#";
                mBuffer = buffer.getBytes();
                DatagramPacket data = new DatagramPacket(mBuffer, mBuffer.length, mBroadcastAddress, mPort);
                mDatagramSocket.send(data);
            }
            catch (IOException e) {
                Log.e(TAG, "Exception inside runnable");
                e.printStackTrace();
                mDatagramSocket.close();
            }
            
            long endTime = SystemClock.uptimeMillis();
            long diffTime = endTime - startTime;
            try {
                if (TIME > diffTime)
                    Thread.sleep(TIME - diffTime);
            }
            catch(InterruptedException e) {
                e.printStackTrace();
                mDatagramSocket.close();
            }
        }
    }
}
