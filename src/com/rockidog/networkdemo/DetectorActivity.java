package com.rockidog.networkdemo;

import com.rockidog.networkdemo.utils.Detector;

import org.opencv.android.JavaCameraView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import java.io.InputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileOutputStream;

public class DetectorActivity extends Activity {
    private Detector mNativeDetector;
    private CameraView mCameraView;
    private File mCascadeFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
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
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        mNativeDetector = new Detector(this, mCascadeFile.getAbsolutePath());
        mCameraView = new CameraView(this, JavaCameraView.CAMERA_ID_FRONT);
        mCameraView.setCvCameraViewListener(mNativeDetector);
        setContentView(mCameraView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.enableView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView.disableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraView.disableView();
    }
}
