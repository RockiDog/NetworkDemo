package com.rockidog.networkdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends Activity {
    public static Socket mSocket = null;
    public static String mIP = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIP = getString(R.string.init_ip);
        setContentView(R.layout.activity_main);
    }

    public void onConnectClick(View view) {
        EditText ipText = (EditText) findViewById(R.id.ipText);
        mIP = ipText.getText().toString();
        new ConnectTask().execute(mIP);
    }

    public void onSendClick(View view) {
        EditText contentText = (EditText) findViewById(R.id.contentText);
        String content = contentText.getText().toString();
        new SendTask().execute(content);
    }

    public void onStartClick(View view) {
        Intent intent = new Intent(this, PanelActivity.class);
        PanelActivity.mIP = MainActivity.mIP;
        //PanelActivity.mSocket = MainActivity.mSocket;
        startActivity(intent);
    }

    private class ConnectTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... IPs) {
            if (0 == IPs[0].length())
                return new String("请输入服务器地址！");
            
            try {
                mSocket = new Socket(IPs[0], 7000);
            }
            catch (UnknownHostException e) {
                return new String("请输入合法的IP地址！");
            }
            catch (IOException e) {
                return new String("连接失败，IP不正确或服务端未开启！");
            }
            
            return new String("连接成功！");
        }
        
        @Override
        protected void onPostExecute(String message) {
            Context context = getApplicationContext();
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    private class SendTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... message) {
            try {
                OutputStream mOutputStream = mSocket.getOutputStream();
                byte[] mBuffer = message[0].getBytes();
                mOutputStream.write(mBuffer);
                mOutputStream.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
                try {
                    mSocket = new Socket(mIP, 7000);
                }
                catch (IOException socketException)
                {
                    socketException.printStackTrace();
                }
            }
            return null;
        }
    }
}

