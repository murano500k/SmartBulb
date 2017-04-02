package com.stc.smartbulb.utils;

import android.content.Context;
import android.support.v7.preference.PreferenceManager;

import com.stc.smartbulb.R;

/**
 * Provides SSIO of current WIFI connection.
 */

public class PrefsUtils {



    public static String getSavedDeviceIP(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c).getString(c.getString(R.string.pref_key_device_ip), null);
    }
    public static String getSavedDevicePort(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c).getString(c.getString(R.string.pref_key_device_port), null);
    }
    public static String getSavedDeviceMac(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c).getString(c.getString(R.string.pref_key_device_mac), null);
    }
    public static String getSavedDeviceName(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c).getString(c.getString(R.string.pref_key_device_name), null);
    }
    public static boolean getSavedTrggerWifiEnabled(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c).getBoolean(c.getString(R.string.pref_key_trigger_wifi_enabled), false);
    }
    public static String getSavedWifiSSID(Context c){
        return PreferenceManager.getDefaultSharedPreferences(c).getString(c.getString(R.string.pref_key_trigger_wifi_ssid), null);
    }


    public static void saveDeviceIP(Context c, String val){
        PreferenceManager.getDefaultSharedPreferences(c).edit().putString(c.getString(R.string.pref_key_device_ip), null).apply();
    }
    public static void saveDevicePort(Context c, String val){
        PreferenceManager.getDefaultSharedPreferences(c).edit().putString(c.getString(R.string.pref_key_device_port), null).apply();
    }
    public static void saveDeviceMac(Context c, String val){
        PreferenceManager.getDefaultSharedPreferences(c).edit().putString(c.getString(R.string.pref_key_device_mac), null).apply();
    }
    public static void saveDeviceName(Context c, String val){
        PreferenceManager.getDefaultSharedPreferences(c).edit().putString(c.getString(R.string.pref_key_device_name), null).apply();
    }
    public static void saveWifiSSID(Context c, String val){
        PreferenceManager.getDefaultSharedPreferences(c).edit().putString(c.getString(R.string.pref_key_trigger_wifi_ssid), null).apply();
    }



}
