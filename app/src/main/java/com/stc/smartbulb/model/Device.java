package com.stc.smartbulb.model;

import android.content.Context;

import com.stc.smartbulb.utils.PrefsUtils;

import java.util.HashMap;

/**
 * Created by artem on 3/31/17.
 */

public class Device {
    private String ip;
    private String port;
    private String name;
    private String mac;
    private String ssid;
    private static Device instance;

    public Device(final String ip, final String port, final String name, final String mac, final String ssid) {
        this.ip = ip;
        this.port = port;
        this.name = name;
        this.mac = mac;
        this.ssid = ssid;
    }

    public Device(final HashMap<String, String> bulbInfo) {
        // TODO: 3/31/17 create
    }

    public String getIp() {
        return ip;
    }

    public void setIp(final String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(final String port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(final String mac) {
        this.mac = mac;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(final String ssid) {
        this.ssid = ssid;
    }

    public static Device getInstance(Context context){
        if(instance==null){
            if(PrefsUtils.getSavedDeviceIP(context)!=null && PrefsUtils.getSavedDeviceIP(context).length()>0 &&
                    PrefsUtils.getSavedDevicePort(context)!=null && PrefsUtils.getSavedDevicePort(context).length()>0 &&
                    PrefsUtils.getSavedDeviceName(context)!=null && PrefsUtils.getSavedDeviceName(context).length()>0 &&
                    PrefsUtils.getSavedDeviceMac(context)!=null && PrefsUtils.getSavedDeviceMac(context).length()>0 &&
                    PrefsUtils.getSavedWifiSSID(context)!=null && PrefsUtils.getSavedWifiSSID(context).length()>0 ){
                instance=new Device(PrefsUtils.getSavedDeviceIP(context),
                        PrefsUtils.getSavedDevicePort(context),
                        PrefsUtils.getSavedDeviceName(context),
                        PrefsUtils.getSavedDeviceMac(context),
                        PrefsUtils.getSavedWifiSSID(context)
                );
            }
        }
        return instance;
    }

    boolean getState() {
        // TODO: 4/3/17  
        return false;
        
    }
}
