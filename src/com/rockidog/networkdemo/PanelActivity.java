package com.rockidog.networkdemo;

import com.rockidog.networkdemo.PanelView;
import com.rockidog.networkdemo.PanelView.ActionListener;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class PanelActivity extends Activity implements Runnable {
    private static final int TIME = 30;
    private DatagramSocket mDatagramSocket = null;
    private InetAddress mLocalAddress = null;
    private int mPort = 7001;
    private byte[] mBuffer = null;

    private Thread mSocketThread = null;
    private boolean isPaused = false;
    private float[] mDirection = {0, 0};
    private float[] mSpeed = {0, 0};
    private int mButton = -1;
    private int mPower = 0;
    private boolean isButtonClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PanelView mPanelView = new PanelView(this);
        mPanelView.setActionListener(new ActionListener() {
            @Override
            public void onJoystickPositionChanged(int joystick, float radian, float speed) {
                mDirection[joystick] = radian;
                mSpeed[joystick] = speed;
            }
            
            @Override
            public void onButtonClicked(int button, int power) {
                mButton = button;
                mPower = power;
                isButtonClicked = true;
            }
        });
        setContentView(mPanelView);
        
        mSocketThread = new Thread(this);
        mSocketThread.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPaused = true;
    }

    @Override
    public void run() {
        String buffer = null;
        DatagramPacket data = null;
        try {
            mLocalAddress = InetAddress.getByName("255.255.255.255");
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
                // N: Number of joystick (0: Left, 1: Right)
                // D: Direction in angle (by 0.01 degree, [0, 36000])
                // S: Speed of vehicle in percent ([0, 100])
                // B: Button (0: shooting button, 1: dribbling button)
                // P: Power in percent ([0, 100])
                buffer = "N0" + "D" + Integer.toString((int) mDirection[0]) + "S" + Integer.toString((int) mSpeed[0]) + "#";
                mBuffer = buffer.getBytes();
                data = new DatagramPacket(mBuffer, buffer.length(), mLocalAddress, mPort);
                mDatagramSocket.send(data);
                
                buffer = "N1" + "D" + Integer.toString((int) mDirection[1]) + "S" + Integer.toString((int) mSpeed[1]) + "#";
                mBuffer = buffer.getBytes();
                data = new DatagramPacket(mBuffer, buffer.length(), mLocalAddress, mPort);
                mDatagramSocket.send(data);
                
                if (true == isButtonClicked) {
                    buffer = "B" + mButton + "P" + mPower + "#";
                    mBuffer = buffer.getBytes();
                    data = new DatagramPacket(mBuffer, buffer.length(), mLocalAddress, mPort);
                    mDatagramSocket.send(data);
                    isButtonClicked = false;
                }
            }
            catch (IOException e) {
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
