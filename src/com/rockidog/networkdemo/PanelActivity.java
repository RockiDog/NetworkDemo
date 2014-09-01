package com.rockidog.networkdemo;

import com.rockidog.networkdemo.PanelView;
import com.rockidog.networkdemo.PanelView.JoystickListener;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class PanelActivity extends Activity {
    public static Socket mSocket = null;
    public static String mIP = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PanelView mPanelView = new PanelView(this);
        mPanelView.setJoystickListener(new JoystickListener() {
            @Override
            public void onJoystickPositionChanged(float radian, float speed) {
                int angle = (int) ((-radian / Math.PI) * 180);
                if (0 > angle)
                    angle += 360;
                new SendTask().execute(Integer.toString(angle), Integer.toString((int) speed));
            }
        });
        setContentView(mPanelView);
    }

    private class SendTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... message) {
            try {
                OutputStream mOutputStream = mSocket.getOutputStream();
                String buffer = "$" + message[0] + "_" + message[1] + "#";
                byte[] mBuffer = buffer.getBytes();
                mOutputStream.write(mBuffer);
                mOutputStream.flush();
            }
            catch (IOException e) {
                System.out.println(e.getMessage());
                try {
                    mSocket = new Socket(mIP, 7000);
                }
                catch (IOException socketException)
                {
                    System.out.println(socketException.getMessage());
                }
            }
            return null;
        }
    }
}
