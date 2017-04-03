package com.stc.smartbulb.model;

import android.util.Log;

import com.stc.smartbulb.search.SearchContract;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by artem on 4/3/17.
 */

public class DeviceSearcher {
    private SearchContract.Presenter mPresenter;
    private Device mDevice;
    private static final String TAG = "Searcher";

    private static final String UDP_HOST = "239.255.255.250";
    private static final int UDP_PORT = 1982;
    private static final String message = "M-SEARCH * HTTP/1.1\r\n" +
            "HOST:239.255.255.250:1982\r\n" +
            "MAN:\"ssdp:discover\"\r\n" +
            "ST:wifi_bulb\r\n";
    private DatagramSocket mDSocket;
    private static final int MSG_CONNECT_SUCCESS = 0;
    private static final int MSG_CONNECT_FAILURE = 1;
    private Disposable searchDisposable;

    public boolean isSearching(){
       return searchDisposable!=null;
    }
    public DeviceSearcher(){

    }
    public void search(Consumer<Device> consumer){
        if(searchDisposable!=null && !searchDisposable.isDisposed())
            searchDisposable.dispose();
        searchDisposable=searchObservable().subscribe(consumer);
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

    void stopSearch() {

    }
}
