package com.stc.smartbulb.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.stc.smartbulb.utils.PrefsUtils;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkChangeReceiver";


    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.d(TAG, "onReceive: "+isNetworkConnected(context));
    }

    public static String getConnectedWifiSsid(Context context) {
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifi.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null) {
                return connectionInfo.getSSID().replaceAll("\"", "");
            }
        }
        return null;
    }

    public static boolean isMyNetworkConnected(Context context){
        String savedWifiSsid = PrefsUtils.getSavedWifiSsid(context);
        if(savedWifiSsid==null)return false;
        else if(TextUtils.equals(savedWifiSsid,getConnectedWifiSsid(context))) return true;
        else return false;
    }
    private boolean isNetworkConnected(Context context) {
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo cellular = connec.getActiveNetworkInfo();
        return cellular.isConnected();
    }

}
/*private void listenWifiChanges(){
        Log.d(TAG, "listenWifiChanges");
        if(mReceiver ==null) mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String ssid = NetworkChangeReceiver.getConnectedWifiSsid(context);
                Log.d(TAG, "onReceive ssid="+ssid);
                //if(ssid!=null && mPresenter!=null) mPresenter.start();
            }
        };
        registerReceiver(mReceiver, new IntentFilter(CONNECTIVITY_ACTION));
    }
    private void dontListenWifiChanges(){
        Log.d(TAG, "dontListenWifiChanges");
        if(mReceiver !=null) {
            unregisterReceiver(mReceiver);
            mReceiver =null;
        }
    }*/