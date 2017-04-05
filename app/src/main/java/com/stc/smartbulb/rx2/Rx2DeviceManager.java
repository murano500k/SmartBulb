package com.stc.smartbulb.rx2;

import android.util.Log;

import com.stc.smartbulb.model.Device;

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
    private static final String TAG = "Rx2Model";
    private static final String UDP_HOST = "239.255.255.250";
    private static final int UDP_PORT = 1982;
    private static final String message = "M-SEARCH * HTTP/1.1\r\n" +
            "HOST:239.255.255.250:1982\r\n" +
            "MAN:\"ssdp:discover\"\r\n" +
            "ST:wifi_bulb\r\n";
    private static final String CMD_ON = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"on\",\"smooth\",500]}\r\n" ;
    private static final String CMD_OFF = "{\"id\":%id,\"method\":\"set_power\",\"params\":[\"off\",\"smooth\",500]}\r\n" ;

    private DatagramSocket dSocket;

    public void writeCmd(boolean cmdB, Socket socket) throws IOException {
        Log.d(TAG, "writeCmd: "+cmdB);
        BufferedOutputStream bos =new BufferedOutputStream(socket.getOutputStream());
        String cmd = cmdB ? CMD_ON : CMD_OFF;
        bos.write(cmd.getBytes());
        bos.flush();
    }

    public Device searchDevice() throws Exception {
        cancelSearch();
        Log.d(TAG, "getSearchDeviceObservable");
        dSocket = new DatagramSocket();
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
        return !bulbInfo.isEmpty() ? new Device(bulbInfo) : null;
    }
    public Socket connectToDevice(Device device) throws IOException {
        Log.d(TAG, "connectToDevice: "+device.toString());
        Socket socket = new Socket(device.getIp(), Integer.parseInt(device.getPort()));
        socket.setKeepAlive(true);
        return socket;
    }

    public void cancelSearch() {
        if(dSocket!=null && dSocket.isConnected()) dSocket.close();
    }
}
