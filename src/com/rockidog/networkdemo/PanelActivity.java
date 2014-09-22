package com.rockidog.networkdemo;

import com.rockidog.networkdemo.PanelView;
import com.rockidog.networkdemo.PanelView.ActionListener;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

//import java.io.OutputStream;
//import java.lang.NullPointerException;
//import java.net.Socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class PanelActivity extends Activity {
    //public static Socket mSocket = null;
    public static String mIP = null;
    public static DatagramSocket mDatagramSocket = null;
    public static InetAddress mLocalAddress = null;
    public static int mPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PanelView mPanelView = new PanelView(this);
        mPanelView.setActionListener(new ActionListener() {
            @Override
            public void onJoystickPositionChanged(int joystick, float radian, float speed) {
                new SendTask().execute(Integer.toString(joystick), Integer.toString((int) radian), Integer.toString((int) speed));
            }
            
            @Override
            public void onButtonClicked(int button, int power) {
                new SendTask().execute(Integer.toString(button), Integer.toString(power));
            }
        });
        setContentView(mPanelView);
    }

    private class SendTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... message) {
            try {
                mLocalAddress = InetAddress.getByName("255.255.255.255");
                mDatagramSocket = new DatagramSocket();
            }
            catch (SocketException s) {
                s.printStackTrace();
            }
            catch (UnknownHostException u) {
                u.printStackTrace();
            }
            
            String buffer;
            if (3 == message.length)
                buffer = "N" + message[0] + "D" + message[1] + "S" + message[2] + "#";
            else
                buffer = "B" + message[0] + "P" + message[1] + "#";
            byte[] mBuffer = buffer.getBytes();
            mPort = 7001;
            DatagramPacket mData = new DatagramPacket(mBuffer, buffer.length(), mLocalAddress, mPort);
            
            try {
                mDatagramSocket.send(mData);
                mDatagramSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
            /*
            try {
                OutputStream mOutputStream = mSocket.getOutputStream();
                String buffer;
                if (3 == message.length)
                    buffer = "N" + message[0] + "D" + message[1] + "S" + message[2] + "#";
                else
                    buffer = "B" + message[0] + "P" + message[1] + "#";
                byte[] mBuffer = buffer.getBytes();
                mOutputStream.write(mBuffer);
                mOutputStream.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
                try {
                    mSocket = new Socket(mIP, 7000);
                }
                catch (IOException s) {
                    s.printStackTrace();
                }
            }
            catch (NullPointerException e) {
                e.printStackTrace();
                try {
                    mSocket = new Socket(mIP, 7000);
                }
                catch (IOException socketException) {
                    e.printStackTrace();
                }
            }
            return null;
            */
        }
    }
}
