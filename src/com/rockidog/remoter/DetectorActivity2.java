/*
package com.rockidog.remoter;

import com.rockidog.remoter.utils.Detector2;
import com.rockidog.remoter.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.NativeCameraView;
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
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;
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
import java.util.LinkedList;
import static java.lang.Math.*;

public class DetectorActivity2 extends Activity implements Runnable, CvCameraViewListener2 {
  private static final int TIME = 30;
  private static final double ATTENUATION = 0.913;
  private static final double MAX_ROTATE_SPEED = 20;
  private static final double MAX_TRANSLATE_SPEED = 10;
  private static final int LOST_TOLERANCE = 33;
  private static final String TAG = "Detector2";
  private static final double VIEWING_ANGLE = 1.169;

  private Rect mMaxFace;
  private Rect mThisFace;
  private Rect mLastFace;
  private Point mFaceCenter;
  private Point mCenter;
  private int mThisFaceArea;
  private int mLastFaceArea;
  private int mLostCycles;
  private boolean isLost;

  private int mPort = 7001;
  private DatagramSocket mDatagramSocket;
  private InetAddress mBroadcastAddress;
  private byte[] mBuffer;
  private Thread mSocketThread;
  private boolean isPaused;

  private Scalar mRectColor = new Scalar(0, 255, 0, 255);
  private int mRectThickness = 3;
  private Mat mRgbaFrame;
  private Mat mGrayFrame;
  private File mCascadeFile;
  private Detector2 mFrontalDetector;
  private NativeCameraView mCameraView;

  private double mViewWidth;
  private long mExitTime;
  private boolean isBackClicked;

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
    mCameraView = new NativeCameraView(this, NativeCameraView.CAMERA_ID_FRONT);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    Log.i(TAG, "Trying to set camera view listener");
    mCameraView.setCvCameraViewListener(this);
    mCameraView.enableFpsMeter();
    setContentView(mCameraView);
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
    mThisFace = null;
    mLastFace = null;
    isBackClicked = false;
  }

  @Override
  public void onDestroy() {
    Log.i(TAG, "Inside onDestroy");
    super.onDestroy();
    if (null != mCameraView)
      mCameraView.disableView();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {  // Double click BACK to quit
    if(KeyEvent.KEYCODE_BACK == keyCode && KeyEvent.ACTION_DOWN == event.getAction()) {
      if((System.currentTimeMillis() - mExitTime) > 2000) {
        Toast.makeText(getApplicationContext(), "再按一次退出人脸控制", Toast.LENGTH_SHORT).show();
        mExitTime = System.currentTimeMillis();
      } else {
        isBackClicked = true;
      }
      return true;
    }
    return false;
  }

  @Override
  public void onCameraViewStarted(int width, int height) {
    Log.i(TAG, "onCameraViewStarted");
    mViewWidth = width;
    mCenter = new Point(width / 2, height / 2);
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
    // Capture frame
    Core.flip(inputFrame.rgba(), mRgbaFrame, 1);
    mGrayFrame = inputFrame.gray();
    MatOfRect frontal = new MatOfRect();
    
    // Detect faces
    if (null != mFrontalDetector)
      mFrontalDetector.Detect(mGrayFrame, frontal);
    
    // Update max face
    LinkedList<Rect> facesList = new LinkedList<Rect>(frontal.toList());
    if (0 != facesList.size()) {
      isLost = false;
      mMaxFace = facesList.get(0);
      for (Rect face: facesList)
        if (mMaxFace.width * mMaxFace.height < face.width * face.height)
          mMaxFace = face;
      mLastFace = mThisFace;
      mLastFaceArea = mThisFaceArea;
      mThisFace = mMaxFace;
      mThisFaceArea = mMaxFace.width * mMaxFace.height;
    } else {
      isLost = true;
      mThisFace = mLastFace;
      mThisFaceArea = mLastFaceArea;
    }
    
    mFaceCenter = mCenter;
    if (null != mThisFace) {
      double brX = mViewWidth - mThisFace.br().x;
      double brY = mThisFace.br().y;
      double tlX = mViewWidth - mThisFace.tl().x;
      double tlY = mThisFace.tl().y;
      mFaceCenter = new Point((brX + tlX) / 2, (brY + tlY) / 2);
      Core.rectangle(mRgbaFrame, new Point(tlX, tlY), new Point(brX, brY), mRectColor, mRectThickness);
      Core.putText(mRgbaFrame, mFaceCenter.toString(), mFaceCenter, Core.FONT_HERSHEY_PLAIN, 2, mRectColor, mRectThickness);
    }
    
    return mRgbaFrame;
  }

  @Override
  public void run() {
    try {
      mBroadcastAddress = InetAddress.getByName(mIP);
      mDatagramSocket = new DatagramSocket();
    } catch (SocketException s) {
      s.printStackTrace();
      mDatagramSocket.close();
    } catch (UnknownHostException u) {
      u.printStackTrace();
      mDatagramSocket.close();
    }
    
    int[] direction = {0, 0};
    double[] speed = {MAX_ROTATE_SPEED, MAX_ROTATE_SPEED};
    while (false == isPaused) {
      long startTime = SystemClock.uptimeMillis();
      
      try {
        if (false == isBackClicked) {
          if (null == mFaceCenter) {
            mLostCycles = 0;
            speed[0] = 0;
            speed[1] = 0;
            direction[0] = 0;
            direction[1] = 0;
          } else if (true == isLost) {           // Face lost
            ++mLostCycles;
            if (mLostCycles < LOST_TOLERANCE) {  // Lost time not long
              speed[0] *= ATTENUATION;
              speed[1] *= ATTENUATION;
            } else {                             // Lost time too long
              speed[0] = 0;
              speed[1] = 0;
            }
          } else {                               // Face not lost
            mLostCycles = 0;
            double k = atan((mFaceCenter.x - mCenter.x) / mViewWidth * 2 / tan(0.585)) / (VIEWING_ANGLE / 2);
            speed[1] = abs(MAX_ROTATE_SPEED * k);
            speed[0] = abs((mFaceCenter.x - mCenter.x) / mViewWidth * 2 * MAX_TRANSLATE_SPEED);
            direction[0] = 0;
            direction[0] = 0;
            if (mThisFaceArea < mLastFaceArea * 0.8) direction[0] = 0;      // Move forward
            if (mThisFaceArea > mLastFaceArea * 1.2) direction[0] = 31415;  // Get back
            if (mFaceCenter.x < mCenter.x) direction[1] = 15707;  // Turn right
            if (mFaceCenter.x > mCenter.x) direction[1] = 47123;  // Turn left
          }
          
          // L: Left joystick
          // R: Right joystick, not used here
          // D: Direction in angle (by 0.01 degree, [0, 36000])
          // S: Speed of vehicle in percent ([0, 100])
          // B: Button (0: shooting button, 1: dribbling button, 2: invalid button), not used here
          // P: Power in percent ([0, 100]), not used here
          mBuffer = ("LD" + direction[0] + "S" + (int)speed[0] + "RD" + direction[1] + "S" + (int)speed[1] + "B2P0#").getBytes();
          DatagramPacket data = new DatagramPacket(mBuffer, mBuffer.length, mBroadcastAddress, mPort);
          mDatagramSocket.send(data);
        } else {
          int it = 10;
          while (0 != it--) {
            mBuffer = "LD0S0RD0S0B2P0#".getBytes();
            DatagramPacket data = new DatagramPacket(mBuffer, mBuffer.length, mBroadcastAddress, mPort);
            mDatagramSocket.send(data);
            try {
              Thread.sleep(30);
            } catch (InterruptedException e) {
              e.printStackTrace();
              mDatagramSocket.close();
            }
          }
          finish();
        }
      } catch (IOException e) {
        Log.e(TAG, "Exception inside runnable");
        e.printStackTrace();
        mDatagramSocket.close();
      }
      
      long endTime = SystemClock.uptimeMillis();
      long diffTime = endTime - startTime;
      try {
        if (TIME > diffTime)
          Thread.sleep(TIME - diffTime);
      } catch(InterruptedException e) {
        e.printStackTrace();
        mDatagramSocket.close();
      }
    }
  }
}
*/
package com.rockidog.remoter;

import com.rockidog.remoter.utils.Detector2;
import com.rockidog.remoter.utils.Predictor;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.NativeCameraView;
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
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;
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
import java.util.LinkedList;
import static java.lang.Math.*;

public class DetectorActivity2 extends Activity implements Runnable, CvCameraViewListener2 {
  private static final int TIME = 30;
  private static final double ATTENUATION = 0.913;
  private static final double MAX_ROTATE_SPEED = 20;
  //private static final double MAX_TRANSLATE_SPEED = 10;
  private static final int LOST_TOLERANCE = 33;
  private static final String TAG = "Detector2";
  private static final double VIEWING_ANGLE = 1.169;

  private int mPort = 7001;
  private DatagramSocket mDatagramSocket;
  private InetAddress mBroadcastAddress;
  private byte[] mBuffer;
  private Thread mSocketThread;
  private boolean isPaused;
  private long mExitTime;
  private boolean isBackClicked;

  private Scalar mColorGreen = new Scalar(0, 255, 0, 255);
  private Scalar mColorRed = new Scalar(255, 0, 0, 255);
  private int mRectThickness = 3;
  private Mat mRgbaFrame;
  private Mat mGrayFrame;
  private File mCascadeFile;
  private Detector2 mFrontalDetector;
  private NativeCameraView mCameraView;

  private Point mCenter;
  private double mViewWidth;
  //private double mViewHeight;
  private double mViewArea;
  private boolean isFrameStarted;

  private Rect mThisFace;
  private Rect mLastFace;
  private Rect mTrueFace;
  private Point mThisFaceCenter;
  private Point mTrueFaceCenter;
  private int mLostCycles;
  private Predictor mPredictor;

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
    
    mCameraView = new NativeCameraView(this, NativeCameraView.CAMERA_ID_FRONT);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    mCameraView.setCvCameraViewListener(this);
    mCameraView.enableFpsMeter();
    setContentView(mCameraView);
    
    mLostCycles = 0;
    mPredictor = new Predictor(LOST_TOLERANCE);
    mLastFace = new Rect();
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
    isBackClicked = false;
    isFrameStarted = false;
    
    mSocketThread = new Thread(this);
    mSocketThread.start();
  }

  @Override
  public void onDestroy() {
    Log.i(TAG, "Inside onDestroy");
    super.onDestroy();
    if (null != mCameraView)
      mCameraView.disableView();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {  // Double click BACK to quit
    if(KeyEvent.KEYCODE_BACK == keyCode && KeyEvent.ACTION_DOWN == event.getAction()) {
      if((System.currentTimeMillis() - mExitTime) > 2000) {
        Toast.makeText(getApplicationContext(), "再按一次退出人脸控制", Toast.LENGTH_SHORT).show();
        mExitTime = System.currentTimeMillis();
      } else {
        isBackClicked = true;
      }
      return true;
    }
    return false;
  }

  @Override
  public void onCameraViewStarted(int width, int height) {
    Log.i(TAG, "onCameraViewStarted");
    mCenter = new Point(width / 2, height / 2);
    mViewWidth = width;
    //mViewHeight = height;
    mViewArea = width * height;
    
    mGrayFrame = new Mat();
    mRgbaFrame = new Mat();
    isPaused = false;
    isFrameStarted = true;
  }

  @Override
  public void onCameraViewStopped() {
    Log.i(TAG, "onCameraViewStopped");
    mGrayFrame.release();
    mRgbaFrame.release();
    isPaused = true;
    isFrameStarted = false;
  }

  @Override
  public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
    // Capture frame
    Core.flip(inputFrame.rgba(), mRgbaFrame, 1);
    mGrayFrame = inputFrame.gray();
    MatOfRect frontal = new MatOfRect();
    
    // Detect faces
    if (null != mFrontalDetector)
      mFrontalDetector.Detect(mGrayFrame, frontal);
    LinkedList<Rect> facesList = new LinkedList<Rect>(frontal.toList());
    
    // Get max face
    double maxFaceArea = 0;
    mThisFace = null;
    mThisFaceCenter = null;
    for (Rect face: facesList) {
      if (maxFaceArea < face.area()) {
        maxFaceArea = face.area();
        if (maxFaceArea > mViewArea * 0.05) {
          mThisFace = face;
          mThisFaceCenter = new Point(face.x + face.width / 2, face.y + face.height / 2);
        }
      }
    }
    if (null != mThisFace)
      mLastFace = mThisFace;
    
    // Draw the rectangle
    if (null != mTrueFaceCenter) {
      double brX = mViewWidth - (mTrueFace.x + mTrueFace.width);
      double brY = mTrueFace.y + mTrueFace.height;
      double tlX = mViewWidth - mTrueFace.x;
      double tlY = mTrueFace.y;
      if (null != mThisFace)
        Core.rectangle(mRgbaFrame, new Point(tlX, tlY), new Point(brX, brY), mColorGreen, mRectThickness);
      else
        Core.rectangle(mRgbaFrame, new Point(tlX, tlY), new Point(brX, brY), mColorRed, mRectThickness);
      Core.putText(mRgbaFrame, mTrueFace.tl().toString(), new Point(tlX, tlY), Core.FONT_HERSHEY_PLAIN, 2, mColorGreen, mRectThickness);
    }
    
    return mRgbaFrame;
  }

  @Override
  public void run() {
    try {
      mBroadcastAddress = InetAddress.getByName(mIP);
      mDatagramSocket = new DatagramSocket();
    } catch (SocketException s) {
      s.printStackTrace();
      mDatagramSocket.close();
    } catch (UnknownHostException u) {
      u.printStackTrace();
      mDatagramSocket.close();
    }
    
    int[] direction = {0, 0};
    double[] speed = {MAX_ROTATE_SPEED, MAX_ROTATE_SPEED};
    while (true) {
      if (true == isPaused)
        break;
      if (false == isFrameStarted) {
        try {
          Thread.sleep(TIME);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        continue;
      }
      
      long startTime = SystemClock.uptimeMillis();
      
      if ((null != mThisFace && null != mThisFaceCenter) || mLostCycles < LOST_TOLERANCE) {
        if (null == mThisFace) {                                      // Face lost, predict
          ++mLostCycles;
          mPredictor.doPredict();                                     // Predict
          double x = mPredictor.getX();
          double y = mPredictor.getY();
          Point tl = new Point(x - mLastFace.width / 2, y - mLastFace.height / 2);
          Point br = new Point(x + mLastFace.width / 2, y + mLastFace.height / 2);
          mTrueFaceCenter = new Point(x, y);
          mTrueFace = new Rect(tl, br);
        } else if (null != mThisFaceCenter) {                                                      // Face not lost
          mLostCycles = 0;
          mPredictor.doUpdate(mThisFaceCenter.x, mThisFaceCenter.y);  // Update
          mTrueFaceCenter = mThisFaceCenter;
          mTrueFace = mThisFace;
        }
        
        double k = atan((mTrueFaceCenter.x - mCenter.x) / mViewWidth * 2 / tan(0.585)) / (VIEWING_ANGLE / 2);
        speed[1] = abs(MAX_ROTATE_SPEED * k);
        speed[0] = 0;
        direction[0] = 0;
        direction[0] = 0;
        if (mTrueFaceCenter.x < mCenter.x) direction[1] = 15707;      // Turn right
        if (mTrueFaceCenter.x > mCenter.x) direction[1] = 47123;      // Turn left
      } else {                                                        // Face lost too many cycles, attenuate
        speed[0] *= ATTENUATION;
        speed[1] *= ATTENUATION;
      }
      
      try {
        /**
         *  L: Left joystick
         *  R: Right joystick, not used here
         *  D: Direction in angle (by 0.01 degree, [0, 36000])
         *  S: Speed of vehicle in percent ([0, 100])
         *  B: Button (0: shooting button, 1: dribbling button, 2: invalid button), not used here
         *  P: Power in percent ([0, 100]), not used here
        */
        
        if (true == isBackClicked) {
          int it = 10;
          while (0 != it--) {
            mBuffer = "LD0S0RD0S0B2P0#".getBytes();
            DatagramPacket data = new DatagramPacket(mBuffer, mBuffer.length, mBroadcastAddress, mPort);
            mDatagramSocket.send(data);
            try {
              Thread.sleep(30);
            } catch (InterruptedException e) {
              e.printStackTrace();
              mDatagramSocket.close();
            }
          }
          finish();
        }
        
        mBuffer = ("LD" + direction[0] + "S" + (int)speed[0] + "RD" + direction[1] + "S" + (int)speed[1] + "B2P0#").getBytes();
        DatagramPacket data = new DatagramPacket(mBuffer, mBuffer.length, mBroadcastAddress, mPort);
        mDatagramSocket.send(data);
      } catch (IOException e) {
        Log.e(TAG, "Exception inside runnable");
        e.printStackTrace();
        mDatagramSocket.close();
      }
      
      long endTime = SystemClock.uptimeMillis();
      long diffTime = endTime - startTime;
      try {
        if (TIME > diffTime)
          Thread.sleep(TIME - diffTime);
      } catch (InterruptedException e) {
        e.printStackTrace();
        mDatagramSocket.close();
      }
    }
  }
}
