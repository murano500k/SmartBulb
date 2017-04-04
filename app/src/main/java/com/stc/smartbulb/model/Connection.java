package com.stc.smartbulb.model;

import java.io.BufferedOutputStream;
import java.net.Socket;

/**
 * Created by artem on 4/4/17.
 */

public class Connection {
    private Socket mSocket;
    private BufferedOutputStream mBos;
    private Device mDevice;
    public Connection(Socket socket, BufferedOutputStream bos, Device device) {
        mSocket = socket;
        mBos = bos;
        mDevice=device;

    }

    public Socket getSocket() {
        return mSocket;
    }

    public BufferedOutputStream getBos() {
        return mBos;
    }

    public Device getDevice() {
        return mDevice;
    }
}
