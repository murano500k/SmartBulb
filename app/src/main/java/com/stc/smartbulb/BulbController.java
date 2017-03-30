package com.stc.smartbulb;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

import io.reactivex.Single;
import io.reactivex.functions.BiConsumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by artem on 3/29/17.
 */

public class BulbController {
    private final ControllerCallback callback;
    private String TAG = "Control";

    private static final int MSG_CONNECT_SUCCESS = 0;
    private static final int MSG_CONNECT_FAILURE = 1;
    private static final String CMD_TOGGLE = "{\"id\":%id,\"method\":\"toggle\",\"params\":[]}\r\n" ;
    private static final String CMD_ON = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"on\",\"smooth\",500]}\r\n" ;
    private static final String CMD_OFF = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"off\",\"smooth\",500]}\r\n" ;
    private static final String CMD_CT = "{\"id\":%id,\"method\":\"set_ct_abx\",\"params\":[%value, \"smooth\", 500]}\r\n";
    private static final String CMD_HSV = "{\"id\":%id,\"method\":\"set_hsv\",\"params\":[%value, 100, \"smooth\", 200]}\r\n";
    private static final String CMD_BRIGHTNESS = "{\"id\":%id,\"method\":\"set_bright\",\"params\":[%value, \"smooth\", 200]}\r\n";
    private static final String CMD_BRIGHTNESS_SCENE = "{\"id\":%id,\"method\":\"set_bright\",\"params\":[%value, \"smooth\", 500]}\r\n";
    private static final String CMD_COLOR_SCENE = "{\"id\":%id,\"method\":\"set_scene\",\"params\":[\"cf\",1,0,\"100,1,%color,1\"]}\r\n";

    private int mCmdId;
    private Socket mSocket;
    private String mBulbIP;
    private int mBulbPort;
    private BufferedOutputStream mBos;
    private BufferedReader mReader;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_CONNECT_FAILURE:

                    Log.e(TAG, "handleMessage: FAIL");
                    break;
                case MSG_CONNECT_SUCCESS:
                    Log.d(TAG, "handleMessage: SUCCESS");
                    break;
            }
        }
    };
    public void switchBulb(boolean val){
        String cmd = parseSwitch(val);
        write(cmd);
    }
    private void write(String cmd){
        Single.just(cmd).subscribeOn(Schedulers.newThread()).observeOn(Schedulers.newThread()).subscribe(new BiConsumer<String, Throwable>() {
            @Override
            public void accept(String s, Throwable throwable) throws Exception {
                if(s==null || throwable!=null) {
                    Log.e(TAG, throwable.getMessage());
                } else {
                    if (mBos != null && mSocket.isConnected()){
                        try {
                            mBos.write(s.getBytes());
                            mBos.flush();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG,"mBos = null or mSocket is closed");
                    }
                }
            }
        });

    }

    private String parseSwitch(boolean on){
        String cmd;
        if (on){
            cmd = CMD_ON.replace("%id", String.valueOf(++mCmdId));
        }else {
            cmd = CMD_OFF.replace("%id", String.valueOf(++mCmdId));
        }
        return cmd;
    }
    public void disconnect(){
        try{
            cmd_run = false;
            if (mSocket!=null)
                mSocket.close();
        }catch (Exception e){

        }
    }
    public BulbController(String ip ,int port, ControllerCallback callback){
        mBulbIP = ip;
        mBulbPort = port;
        this.callback=callback;
        connect();
    }
    private boolean cmd_run = true;
    private void connect(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    cmd_run = true;
                    mSocket = new Socket(mBulbIP, mBulbPort);
                    mSocket.setKeepAlive(true);
                    mBos= new BufferedOutputStream(mSocket.getOutputStream());
                    mHandler.sendEmptyMessage(MSG_CONNECT_SUCCESS);
                    mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    while (cmd_run){
                        try {
                            String value = mReader.readLine();
                            Log.d(TAG, "value = "+value);
                        }catch (Exception e){

                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessage(MSG_CONNECT_FAILURE);
                }
            }
        }).start();
    }
}
