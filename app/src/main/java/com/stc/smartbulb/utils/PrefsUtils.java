package com.stc.smartbulb.utils;

import android.content.Context;
import android.support.v7.preference.PreferenceManager;

/**
 * Provides SSIO of current WIFI connection.
 */

public class PrefsUtils {
    private static final String KEY_SAVED_SSID = "KEY_SAVED_SSID++";
    private static final String KEY_BULB_IP = "KEY_BULB_IP";
    private static final String KEY_BULB_PORT = "KEY_BULB_PORT";

    public static String getSavedNetworkSSID(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c).getString(KEY_SAVED_SSID, null);
    }
    public static String getSavedBulbIP(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c).getString(KEY_BULB_IP, null);
    }
    public static int getSavedBulbPort(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c).getInt(KEY_BULB_PORT, Integer.MAX_VALUE);
    }
}
