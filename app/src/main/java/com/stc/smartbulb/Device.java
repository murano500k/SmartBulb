package com.stc.smartbulb;

import java.util.HashMap;

/**
 * Created by artem on 3/31/17.
 */

public class Device {
    private static final String TAG = "Device";
    private String ip;
    private String port;
    private String name;
    boolean turnedOn;

    public boolean isTurnedOn() {
        return turnedOn;
    }

    public void setTurnedOn(boolean turnedOn) {
        this.turnedOn = turnedOn;
    }

    public Device(final HashMap<String, String> bulbInfo) {
        if(bulbInfo!=null && !bulbInfo.isEmpty()){
            turnedOn=bulbInfo.get("power").contains("on");
            String location = bulbInfo.get("Location");
            String ipPort = location.substring(location.lastIndexOf("/")+1);
            ip= ipPort.substring(0,ipPort.indexOf(":"));
            port=ipPort.substring(ipPort.indexOf(":")+1, ipPort.length());
            name=bulbInfo.get("model");
        }
    }
    /*keyset:{
key:Cache-Control
val: max-age=3600
key:hue
val: 0
key:rgb
val: 0
key:power
val: on
key:ct
val: 4000
key:Ext
val: 
key:fw_ver
val: 45
key:bright
val: 100
key:support
val: get_prop set_default set_power toggle set_bright start_cf stop_cf set_scene cron_add cron_get cron_del set_adjust set_name
key:id
val: 0x0000000000c8f736
key:name
val: 
key:Date
val: 
key:Location
val: yeelight://192.168.0.102:55443
key:color_mode
val: 2
key:Server
val: POSIX UPnP/1.0 YGLC/1
key:sat
val: 0
key:model
val: mono
}*/

    public String getIp() {
        return ip;
    }

    public String getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Device{" +
                "ip='" + ip + '\'' +
                ", port='" + port + '\'' +
                ", name='" + name + '\'' +
                ", turnedOn=" + turnedOn +
                '}';
    }
}
