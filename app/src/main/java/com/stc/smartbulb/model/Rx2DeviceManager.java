package com.stc.smartbulb.model;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;

/**
 * Created by artem on 4/4/17.
 */

public class Rx2DeviceManager {
    private static final String TAG = "Rx2DeviceManager";
    private static final String UDP_HOST = "239.255.255.250";
    private static final int UDP_PORT = 1982;
    private static final String message = "M-SEARCH * HTTP/1.1\r\n" +
            "HOST:239.255.255.250:1982\r\n" +
            "MAN:\"ssdp:discover\"\r\n" +
            "ST:wifi_bulb\r\n";
    public static final String CMD_ON = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"on\",\"smooth\",500]}\r\n" ;
    public static final String CMD_OFF = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"off\",\"smooth\",500]}\r\n" ;
    public static final String CMD_TOGGLE = "{\"id\":%id,\"method\":\"toggle\",\"params\":[]}\r\n" ;
    public static final String CMD_GET_PROP = "{\"id\":%id,\"method\":\"get_prop\",\"params\":[\"power\"]}\r\n" ;


    private DatagramSocket dSocket;
    private int mCmdId;

    public void writeCmd(boolean cmdB, Socket socket) throws IOException {
        BufferedOutputStream bos =new BufferedOutputStream(socket.getOutputStream());
        String cmdTemplate = cmdB ? CMD_ON : CMD_OFF;
        String cmd = cmdTemplate.replace("%id", String.valueOf(++mCmdId));
        Log.d(TAG, "writeCmd: "+cmd);
        bos.write(cmd.getBytes());
        bos.flush();
    }

    public void writeCmd(String cmdTemplate , Socket socket) throws IOException {
        BufferedOutputStream bos =new BufferedOutputStream(socket.getOutputStream());
        String cmd = cmdTemplate.replace("%id", String.valueOf(++mCmdId));
        Log.d(TAG, "writeCmd: "+cmd);
        bos.write(cmd.getBytes());
        bos.flush();
    }

    public Device searchDevice() throws Exception {
        Log.d(TAG, "getSearchDeviceObservable");
        if(dSocket==null || dSocket.isClosed()) dSocket = new DatagramSocket();
        DatagramPacket dpSend = new DatagramPacket(message.getBytes(),
                message.getBytes().length, InetAddress.getByName(UDP_HOST),
                UDP_PORT);
        dSocket.send(dpSend);
        byte[] buf = new byte[1024];
        DatagramPacket dpRecv = new DatagramPacket(buf, buf.length);
        dSocket.receive(dpRecv);
        byte[] bytes = dpRecv.getData();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < dpRecv.getLength(); i++) {
            // parse /r
            if (bytes[i] == 13) {
                continue;
            }
            buffer.append((char) bytes[i]);
        }
        //Log.d("socket", "got message:" + buffer.toString());
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
        Log.d(TAG, "bulbinfo:{");
        for(String key : bulbInfo.keySet()){
            Log.d(TAG, key+" : "+bulbInfo.get(key));
        }
        Log.d(TAG, "}");
        if (!bulbInfo.isEmpty())
        return new Device(bulbInfo);
        throw new NullPointerException();
    }
    public Socket connectToDevice(Device device) throws IOException {
        Log.d(TAG, "connectToDevice: "+device.toString());
        Socket socket = new Socket(device.getIp(), Integer.parseInt(device.getPort()));
        socket.setKeepAlive(true);
        return socket;
    }

    public void cancelSearch() {
        if(dSocket!=null && !dSocket.isClosed()) dSocket.close();
    }
    public Device parseMsg(String msg, Device device) throws Exception{
        Log.d(TAG, "parseMsg: "+msg);
        if(msg.contains("power")) device.setTurnedOn(msg.contains("on"));
        return device;
    }
}
