package com.stc.smartbulb;

import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * Created by artem on 3/29/17.
 */

public class WifiHelper {
    WifiManager wifiManager;
    Context context;

    public WifiHelper(Context context) {
        this.context = context;
        wifiManager=(WifiManager) context.getSystemService(Context.WIFI_SERVICE);

    }
}
