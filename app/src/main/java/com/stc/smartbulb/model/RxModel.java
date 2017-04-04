package com.stc.smartbulb.model;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by artem on 4/4/17.
 */

public class RxModel {
    private static final String TAG = "RxModel";
    private static final String UDP_HOST = "239.255.255.250";
    private static final int UDP_PORT = 1982;
    private static final String message = "M-SEARCH * HTTP/1.1\r\n" +
            "HOST:239.255.255.250:1982\r\n" +
            "MAN:\"ssdp:discover\"\r\n" +
            "ST:wifi_bulb\r\n";
    private static final String CMD_ON = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"on\",\"smooth\",500]}\r\n" ;
    private static final String CMD_OFF = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"off\",\"smooth\",500]}\r\n" ;


    public Observable<Connection> connect(){
        return getSearchDeviceObservable()
                .flatMap(new Function<Device, ObservableSource<Connection>>() {

                    @Override
                    public ObservableSource<Connection> apply(Device device) throws Exception {
                        return getConnectDeviceObservable(device);
                    }

                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
    public Observable<Device> sendCmd(boolean cmd, Connection connection){
        return  getExecuteCmdObservable(connection, cmd)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    Observable<Device> getSearchDeviceObservable(){
        return Observable.fromCallable(new Callable<Device>() {
            @Override
            public Device call() throws Exception {
                Log.d(TAG, "getSearchDeviceObservable");
                DatagramSocket dSocket = new DatagramSocket();
                DatagramPacket dpSend = new DatagramPacket(message.getBytes(),
                        message.getBytes().length, InetAddress.getByName(UDP_HOST),
                        UDP_PORT);
                dSocket.send(dpSend);
                byte[] buf = new byte[1024];
                DatagramPacket dpRecv = new DatagramPacket(buf, buf.length);
                dSocket.receive(dpRecv);
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
                return !bulbInfo.isEmpty() ? new Device(bulbInfo) : null;
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
    Observable<Connection> getConnectDeviceObservable(Device device){
        return Observable.fromCallable(new Callable<Connection>() {
            @Override
            public Connection call() throws Exception {
                Log.d(TAG, "connectToDevice: "+device);
                Socket socket = new Socket(device.getIp(), Integer.parseInt(device.getPort()));
                socket.setKeepAlive(true);
                BufferedOutputStream bufferedOutputStream= new BufferedOutputStream(socket.getOutputStream());
                return new Connection(socket,bufferedOutputStream,device);
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
    Observable<Device> getExecuteCmdObservable(Connection connection, boolean cmdBoolean){
        return Observable.fromCallable(new Callable<Device>() {
            @Override
            public Device call() throws Exception {
                Log.d(TAG, "getExecuteCmdObservable");

                if (connection.getBos()!= null && connection.getSocket().isConnected()){
                    String cmd = cmdBoolean ? CMD_ON : CMD_OFF;
                    connection.getBos().write(cmd.getBytes());
                    connection.getBos().flush();
                    connection.getDevice().setTurnedOn(cmdBoolean);
                }
                return connection.getDevice();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }


}
