package com.stc.smartbulb.model;

import java.util.HashMap;

/**
 * Created by artem on 3/31/17.
 */

public class Device {
    private String ip;
    private String port;
    private String name;
    private String mac;
    boolean turnedOn;

    public boolean isTurnedOn() {
        return turnedOn;
    }

    public void setTurnedOn(boolean turnedOn) {
        this.turnedOn = turnedOn;
    }

    public Device(final HashMap<String, String> bulbInfo) {
        if(bulbInfo!=null && !bulbInfo.isEmpty()){
            name = "";
            for(String key : bulbInfo.keySet()){
                name+= key+": "+bulbInfo.get(key)+"\n";
            }
        }
    }

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


}
