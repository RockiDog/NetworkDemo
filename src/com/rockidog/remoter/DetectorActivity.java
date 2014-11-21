package com.rockidog.remoter;

import com.rockidog.remoter.utils.Detector;
import com.rockidog.remoter.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;

public class DetectorActivity extends Activity {
  private static final String TAG = "Detector";

  private Detector mNativeDetector;
  private JavaCameraView mCameraView;
  private File mCascadeFile;

  private BaseLoaderCallback mCallback = new BaseLoaderCallback(this) {
    @Override
    public void onManagerConnected(int status) {
      Log.i(TAG, "Inside onManagerConnected");
      switch (status) {
        case LoaderCallbackInterface.SUCCESS:
          Log.i(TAG, "Manager connecting succeeded");
          try {
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_profileface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            mCascadeFile = new File(cascadeDir, "cascade");
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[2048];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
              os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
          Log.i(TAG, "Loading library...");
          System.loadLibrary("CvDetector");
          Log.i(TAG, "Initializing native detector...");
          mNativeDetector = new Detector(mCascadeFile.getAbsolutePath());
          mCascadeFile.delete();
          break;
        default:
          Log.e(TAG, "Manager connecting failed");
          super.onManagerConnected(status);
          break;
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.i(TAG, "Inside onCreate");
    super.onCreate(savedInstanceState);
    Log.i(TAG, "Loading OpenCV 2.4.9...");
    if (false == OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mCallback))
      Log.e(TAG, "Cannot initialze OpenCV 2.4.9");
    else
      Log.i(TAG, "OpenCV 2.4.9 initialzed");
    mCameraView = new JavaCameraView(this, JavaCameraView.CAMERA_ID_FRONT);
    mCameraView.setZOrderOnTop(true);
    mCameraView.setCvCameraViewListener(mNativeDetector);
    Log.i(TAG, "Setting camera listener to mNativeDetector");
    setContentView(mCameraView);
  }

  @Override
  protected void onResume() {
    Log.i(TAG, "Inside onResume");
    super.onResume();
    Log.i(TAG, "Trying to enable camera view...");
    mCameraView.enableView();
    Log.i(TAG, "Camera view enabled");
  }

  @Override
  protected void onPause() {
    super.onPause();
    mCameraView.disableView();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (null != mCameraView)
      mCameraView.disableView();
    mNativeDetector.Destroy();
  }
}
