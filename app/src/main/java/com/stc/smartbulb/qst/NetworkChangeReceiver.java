package com.stc.smartbulb.qst;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkChangeReceiver";


    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d(TAG, "onReceive: "+isNetworkConnected(context));
    }

    public static boolean isMyWifiConnected(Context context) {
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null) {
                String ssid = connectionInfo.getSSID();
               /* if (TextUtils.equals(ssid, PrefsUtils.getSavedNetworkSSID(context))) {
                    Log.d(TAG, "connected");
                }*/
            }
        }
        Log.d(TAG, "not connected");
        return false;
    }
    private boolean isNetworkConnected(Context context) {
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo cellular = connec.getActiveNetworkInfo();
        return cellular.isConnected();
    }

}