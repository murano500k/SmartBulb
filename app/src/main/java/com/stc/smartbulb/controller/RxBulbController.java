package com.stc.smartbulb.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.stc.smartbulb.model.Device;
import com.stc.smartbulb.model.NetworkChangeReceiver;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.SingleSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

/**
 * Created by artem on 3/29/17.
 */

public class RxBulbController extends BroadcastReceiver implements ControllerInterface {
    private ControllerCallback callback;
    private Device mDevice;
    private Context context;
    private String TAG = "Control";
    private static final int MSG_SHOWLOG = 0;
    private static final int MSG_FOUND_DEVICE = 1;
    private static final int MSG_DISCOVER_FINISH = 2;
    private static final int MSG_STOP_SEARCH = 3;

    private static final String UDP_HOST = "239.255.255.250";
    private static final int UDP_PORT = 1982;
    private static final String message = "M-SEARCH * HTTP/1.1\r\n" +
            "HOST:239.255.255.250:1982\r\n" +
            "MAN:\"ssdp:discover\"\r\n" +
            "ST:wifi_bulb\r\n";//用于发送的字符串
    private boolean mSeraching = true;

    List<HashMap<String, String>> mDeviceList = new ArrayList<HashMap<String, String>>();
    private boolean mNotify = true;
    private DatagramSocket mDSocket;

    private WifiManager.MulticastLock multicastLock;
    private final NetworkChangeReceiver receiver = new NetworkChangeReceiver();
    private Thread mSearchThread = null;

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


    RxBulbController(final ControllerCallback callback, Context context) {
        this.callback = callback;
        this.context = context;
        callback.setController(this);
        this.mDevice = Device.getInstance(context);
        context.registerReceiver(this, new IntentFilter(CONNECTIVITY_ACTION));
        WifiManager wm = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        multicastLock = wm.createMulticastLock("test");
        multicastLock.acquire();
    }

    public Single<Device> searchObservable(){
        return Single.fromCallable(new Callable<Device>() {
            @Override
            public Device call() throws Exception {
                mDSocket = new DatagramSocket();
                DatagramPacket dpSend = new DatagramPacket(message.getBytes(),
                        message.getBytes().length, InetAddress.getByName(UDP_HOST),
                        UDP_PORT);
                mDSocket.send(dpSend);

                while (true) {
                    byte[] buf = new byte[1024];
                    DatagramPacket dpRecv = new DatagramPacket(buf, buf.length);
                    mDSocket.receive(dpRecv);
                    byte[] bytes = dpRecv.getData();
                    StringBuffer buffer = new StringBuffer();
                    for (int i = 0; i < dpRecv.getLength(); i++) {
                        // parse /r
                        if (bytes[i] == 13) {
                            continue;
                        }
                        buffer.append((char) bytes[i]);
                    }
                    Log.d("socket", "got message:" + buffer.toString());
                    if (!buffer.toString().contains("yeelight")) {
                        throw new Exception("Device not found");
                    }
                    String[] infos = buffer.toString().split("\n");
                    HashMap<String, String> bulbInfo = new HashMap<String, String>();
                    for (String str : infos) {
                        int index = str.indexOf(":");
                        if (index == -1) {
                            continue;
                        }
                        String title = str.substring(0, index);
                        String value = str.substring(index + 1);
                        bulbInfo.put(title, value);
                    }
                    return new Device(bulbInfo);
                }
            }
        }).timeout(2, TimeUnit.SECONDS).subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread());
    }

    public void search(){
        searchObservable().subscribe();
    }
    public void connect(){
        searchObservable().flatMap(new Function<Device, SingleSource<?>>() {
            @Override
            public SingleSource<?> apply(final Device device) throws Exception {
                    mSocket = new Socket(mDevice.getIp(), Integer.parseInt(mDevice.getPort()));
                    mSocket.setKeepAlive(true);
                    mBos= new BufferedOutputStream(mSocket.getOutputStream());
                    mHandler.sendEmptyMessage(MSG_CONNECT_SUCCESS);
                    mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                        try {
                            String value = mReader.readLine();
                            Log.d(TAG, "value = "+value);
                        }catch (Exception e){

                        }



                return null;
            }
        });
    }
    SingleObserver<Device> deviceFoundConsumer(){
        return new SingleObserver<Device>(){

            @Override
            public void onSubscribe(final Disposable d) {
                Log.d(TAG, "search started");
            }

            @Override
            public void onSuccess(final Device value) {
                Log.d(TAG, "onSuccess: "+value);
                callback.deviceFound(value);
            }


            @Override
            public void onError(final Throwable e) {
                callback.deviceNotFound();
            }
        };
    }
    SingleObserver<Device> deviceConnectedConsumer(){
        return new SingleObserver<Device>(){

            @Override
            public void onSubscribe(final Disposable d) {
                Log.d(TAG, "search started");
            }

            @Override
            public void onSuccess(final Device value) {
                Log.d(TAG, "onSuccess: "+value);
                callback.deviceFound(value);
            }


            @Override
            public void onError(final Throwable e) {
                callback.deviceNotFound();
            }
        };
    }

    public void sendCommand(boolean onOff){

    }





    @Override
    public void searchDevice() {

    }

    @Override
    public void sendSwitchCommand(final boolean onOff) {

    }

    @Override
    public void setCallback(final ControllerCallback callback) {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public boolean checkWifiTrigger(final Context c) {
        return false;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if(isMyWifiConnected(context)) {

            searchObservable().subscribe(new Consumer<Device>() {
                @Override
                public void accept(final Device device) throws Exception {
                    if (device != null) {

                    }
                }
            });
        }
    }
    private boolean isMyWifiConnected(Context context) {
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null) {
                String ssid = connectionInfo.getSSID();
               /* if (TextUtils.equals(ssid, PrefsUtils.getSavedNetworkSSID(context))) {
                    Log.d(TAG, "connected");
                }*/
            }
        }
        Log.d(TAG, "not connected");
        return false;
    }
    private boolean isNetworkConnected(Context context) {
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo cellular = connec.getActiveNetworkInfo();
        return cellular.isConnected();
    }
}
