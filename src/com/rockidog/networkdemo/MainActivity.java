package com.rockidog.networkdemo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends Activity {
    private Socket socket = null;
    private OutputStream outputStream = null;
    private byte[] buffer = null;
    private String ip = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    public void onConnectClick(View view) {
        EditText ipText = (EditText) findViewById(R.id.ipText);
        ip = ipText.getText().toString();
        
        try {
            socket = new Socket(ip, 7000);
            if (true == socket.isConnected()) {
                Context context = getApplicationContext();
                String text = new String("Connection established!");
                Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    public void onSendClick(View view) {
        EditText contentText = (EditText) findViewById(R.id.contentText);
        String content = contentText.getText().toString();
        
        if (null != socket) {
            try {
                outputStream = socket.getOutputStream();
                buffer = content.getBytes();
                outputStream.write(buffer);
                outputStream.close();
            }
            catch (IOException e) {
                System.out.println(e.toString());
            }
        }
        else {
            if (null != ip) {
                try {
                    socket = new Socket(ip, 7000);
                    outputStream = socket.getOutputStream();
                    buffer = content.getBytes();
                    outputStream.write(buffer);
                    outputStream.close();
                }
                catch (IOException e) {
                    System.out.println(e.toString());
                }
            }
            else {
                Context context = getApplicationContext();
                String text = new String("No connection established!");
                Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        
        socket = null;
    }
}
