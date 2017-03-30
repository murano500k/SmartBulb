package com.stc.smartbulb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.stc.smartbulb.utils.PrefsUtils;

public class WifiReceiver extends BroadcastReceiver {
    private static final String TAG = "WifiReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: "+intent);

        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if(info != null ) {
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            if(TextUtils.equals(ssid, PrefsUtils.getSavedNetworkSSID(context))){
                Log.d(TAG, "onReceive: "+ssid+" "+info.isConnected());
                BulbSwitchService.startActionFoo(context, info.isConnected());
            }
        }
    }
}