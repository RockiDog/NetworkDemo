package com.rockidog.networkdemo;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends Activity {
    private Socket mSocket = null;
    private OutputStream mOutputStream = null;
    private byte[] mBuffer = null;
    private String mIP = null;
    private Toast mToast = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        if (null == mSocket || true == mSocket.isClosed()) {
            if (null == mIP) {
                EditText ipText = (EditText) findViewById(R.id.ipText);
                mIP = ipText.getText().toString();
            }
            new ConnectTask().execute(mIP);
        }
        new SendTask().execute(content);
    }
    
    private class ConnectTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... IPs) {
            if (0 == IPs[0].length())
                return new String("请输入服务器地址！");
            
            try {
                mSocket = new Socket(IPs[0], 7000);
                if (true == mSocket.isConnected())
                    return new String ("连接成功！");
                else
                    return new String ("连接失败！");
            }
            catch (IOException e) {
                System.out.println(e.toString());
                return new String ("程序异常！");
            }
        }
        
        @Override
        protected void onPostExecute(String message) {
            Context context = getApplicationContext();
            mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            mToast.show();
        }
    }
    
    private class SendTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... message) {
            try {
                mOutputStream = mSocket.getOutputStream();
                mBuffer = message[0].getBytes();
                mOutputStream.write(mBuffer);
                mOutputStream.close();
            }
            catch (IOException e) {
                System.out.println(e.toString());
            }
            finally {
                if (null != mSocket) {
                    try {
                        mSocket.close();
                    }
                    catch (IOException e) {
                        System.out.println(e.toString());
                    }
                }
            }
            return null;
        }
    }
}

