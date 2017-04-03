package com.stc.smartbulb.model;

import android.util.Log;

import org.reactivestreams.Subscription;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;

/**
 * Created by artem on 4/3/17.
 */

public class DeviceController {
    private Device mDevice;
    private boolean isConnected;

    private static final String TAG = "DeviceController";


    private static final String CMD_TOGGLE = "{\"id\":%id,\"method\":\"toggle\",\"params\":[]}\r\n" ;
    private static final String CMD_ON = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"on\",\"smooth\",500]}\r\n" ;
    private static final String CMD_OFF = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"off\",\"smooth\",500]}\r\n" ;
    private static final String CMD_CT = "{\"id\":%id,\"method\":\"set_ct_abx\",\"params\":[%value, \"smooth\", 500]}\r\n";
    private static final String CMD_HSV = "{\"id\":%id,\"method\":\"set_hsv\",\"params\":[%value, 100, \"smooth\", 200]}\r\n";
    private static final String CMD_BRIGHTNESS = "{\"id\":%id,\"method\":\"set_bright\",\"params\":[%value, \"smooth\", 200]}\r\n";
    private static final String CMD_BRIGHTNESS_SCENE = "{\"id\":%id,\"method\":\"set_bright\",\"params\":[%value, \"smooth\", 500]}\r\n";
    private static final String CMD_COLOR_SCENE = "{\"id\":%id,\"method\":\"set_scene\",\"params\":[\"cf\",1,0,\"100,1,%color,1\"]}\r\n";

    private Socket mSocket;
    private BufferedOutputStream mBos;
    private BufferedReader mReader;



    public DeviceController(Device device){
        this.mDevice = device;
        isConnected=false;
    }
    public void disconnect(){
        if(isConnected){
            try {
                if(mSocket!=null && !mSocket.isClosed()) mSocket.close();
                if (mBos != null) mBos.close();
                if (mReader != null) mReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            isConnected=false;
        }
    }

    public PublishProcessor<Boolean> connect(){
        PublishProcessor<Boolean> _resultEmitterSubject = PublishProcessor.create();
        _resultEmitterSubject.doOnSubscribe(new Consumer<Subscription>() {
            @Override
            public void accept(final Subscription subscription) throws Exception {
                if(mDevice==null ) throw  new NullPointerException("no device");
                if(isConnected) return;
                connectToDevice(mDevice);
            }
        })
                .doOnNext(new Consumer<Boolean>() {
                    @Override
                    public void accept(final Boolean aBoolean) throws Exception {
                        sendCmd(aBoolean);
                    }
                })
        .doOnTerminate(new Action() {
            @Override
            public void run() throws Exception {
                disconnect();
            }
        });
        return _resultEmitterSubject;
    }

    public void sendCmd(final Boolean aBoolean) throws IOException{
        Log.d(TAG, "sendCmd: "+aBoolean);
        String cmd = aBoolean ? CMD_ON : CMD_OFF ;
        if (mBos != null && mSocket.isConnected()){
            mBos.write(cmd.getBytes());
            mBos.flush();
        }
    }

    private void connectToDevice(Device device)throws Exception {
        Log.d(TAG, "connectToDevice: "+device);
        mSocket = new Socket(device.getIp(), Integer.parseInt(device.getPort()));
        mSocket.setKeepAlive(true);
        mBos= new BufferedOutputStream(mSocket.getOutputStream());
        mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
        isConnected=true;
        while (isConnected){
            try {
                String value = mReader.readLine();
                Log.d(TAG, "value = "+value);
            }catch (Exception e){

            }
        }

    }

}
