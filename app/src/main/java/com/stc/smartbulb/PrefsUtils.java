package com.stc.smartbulb;

import android.content.Context;
import android.support.v7.preference.PreferenceManager;

/**
 * Provides SSIO of current WIFI connection.
 */

public class PrefsUtils {


    public static boolean getSavedTrggerWifiEnabled(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c).getBoolean(c.getString(R.string.pref_key_trigger_wifi_enabled), false);
    }
    public static void saveTriggerEnabled(Context c, boolean val){
        PreferenceManager.getDefaultSharedPreferences(c).edit().putBoolean(c.getString(R.string.pref_key_trigger_wifi_enabled), val).apply();
    }
    public static String getSavedWifiSsid(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c).getString(c.getString(R.string.pref_key_trigger_wifi_ssid), null);
    }
    public static void saveWifiSSID(Context c, String val){
        PreferenceManager.getDefaultSharedPreferences(c).edit().putString(c.getString(R.string.pref_key_trigger_wifi_ssid), val).apply();
    }

    public static long getSavedTimeOff(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getLong(c.getString(R.string.pref_key_trigger_time_turn_off_when), Long.MIN_VALUE);
    }

    public static void saveTimeOff(Context c, long val) {
        PreferenceManager.getDefaultSharedPreferences(c).edit().putLong(c.getString(R.string.pref_key_trigger_time_turn_off_when), val).apply();
    }


    public static long getSavedTimeOn(Context c) {
        return PreferenceManager.getDefaultSharedPreferences(c).getLong(c.getString(R.string.pref_key_trigger_time_turn_on_when), Long.MIN_VALUE);
    }

    public static void saveTimeOn(Context c, long val) {
        PreferenceManager.getDefaultSharedPreferences(c).edit().putLong(c.getString(R.string.pref_key_trigger_time_turn_on_when), val).apply();
    }
}
