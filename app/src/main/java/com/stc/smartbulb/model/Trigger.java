package com.stc.smartbulb.model;

/**
 * Created by artem on 4/3/17.
 */

public class Trigger {
    private String ssid;
    private boolean enabled;
    private int type;

    private static final int TRIGGER_ON_WIFI_CONNECTED = 983;
    private static final int TRIGGER_ON_TIME_TO_ENABLE = 656;
    private static final int TRIGGER_ON_TIME_TO_DISABLE = 656;

}
