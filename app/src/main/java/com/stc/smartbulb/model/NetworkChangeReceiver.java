package com.stc.smartbulb.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.stc.smartbulb.PrefsUtils;
import com.stc.smartbulb.Rx2BulbContract;
import com.stc.smartbulb.Rx2DeviceManager;

import static android.net.ConnectivityManager.TYPE_WIFI;


public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkChangeReceiver";
    private Context mContext;
    private String mHomeWifi;
    private String mCurrentWifi;
    private Rx2BulbContract.View mView;
    private Rx2BulbContract.Presenter mPresenter;

    public NetworkChangeReceiver() {
    }

    public NetworkChangeReceiver(Context context, Rx2BulbContract.View view, Rx2BulbContract.Presenter presenter) {
        mContext=context;
        mView=view;
        mPresenter=presenter;
        mHomeWifi = PrefsUtils.getSavedWifiSsid(context);
        mCurrentWifi = getConnectedWifiSsid(context);
    }

    public String getStateString(NetworkInfo.State networkState){
        if(mHomeWifi==null) return "Home wifi not set";
        else if(getState() && networkState== NetworkInfo.State.CONNECTED) return mHomeWifi+" connected";
        else return mHomeWifi + " not connected";
    }
    public boolean getState(){
        return mHomeWifi!=null && TextUtils.equals(mHomeWifi, mCurrentWifi);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        /*for(String key : intent.getExtras().keySet()){
            Log.d(TAG, "onReceive: "+key+": "+intent.getExtras().get(key));
        }*/
        Bundle bundle=intent.getExtras();
        NetworkInfo info = (NetworkInfo) bundle.get("networkInfo");
        if(info!=null) {
            if(info.getType()==TYPE_WIFI){
                String ssid = info.getExtraInfo();
                Log.d(TAG, "onReceive: "+ssid+ " "+info.getState().toString());
                if(info.getState()== NetworkInfo.State.CONNECTED) {
                    mCurrentWifi=ssid;
                    mPresenter.sendCmd(Rx2DeviceManager.CMD_GET_PROP);
                }else if(info.getState()== NetworkInfo.State.DISCONNECTED){
                    mView.newState(null, getStateString(NetworkInfo.State.DISCONNECTED));
                    mPresenter.cancel();
                }
            }
        }

    }

    public static String getConnectedWifiSsid(Context context) {
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled()) return null;
        final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        if (connectionInfo != null && connectionInfo.getSSID()!=null) {
            return connectionInfo.getSSID();//.replaceAll("\"", "");
        }
        return null;
    }
}