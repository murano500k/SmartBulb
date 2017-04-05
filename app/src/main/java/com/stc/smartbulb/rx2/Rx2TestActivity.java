package com.stc.smartbulb.rx2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.stc.smartbulb.R;
import com.stc.smartbulb.model.Device;
import com.stc.smartbulb.model.NetworkChangeReceiver;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

public class Rx2TestActivity extends AppCompatActivity implements Rx2Contract.View {
    private static final String TAG = "Rx2TestActivity";
    private Rx2Contract.Presenter mPresenter;
    private FloatingActionButton mFabConnect;
    private TextView mTextDeviceInfo;


    private ProgressBar progress;
    private FloatingActionButton mFabToggle;
    private BroadcastReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx2_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progress = (ProgressBar)findViewById(R.id.progress);
        progress.setVisibility(View.GONE);
        mFabConnect = (FloatingActionButton) findViewById(R.id.fabConnect);
        mFabToggle = (FloatingActionButton) findViewById(R.id.fabToggle);
        mTextDeviceInfo = (TextView) findViewById(R.id.text_device_info);
        new Rx2Presenter(this);
        onUpdate(null, null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mPresenter!=null) mPresenter.finish();
        dontListenWifiChanges();
    }

    @Override
    public void setPresenter(Rx2Contract.Presenter presenter) {
        this.mPresenter=presenter;
    }

    @Override
    public void onUpdate(Device device, String errorMsg) {
        Log.d(TAG, "onUpdateDevice: "+device);
        Log.d(TAG, "onUpdateMsg: "+errorMsg);
        progress.setVisibility(View.GONE);
        if(device==null){
            listenWifiChanges();
            mFabToggle.setVisibility(View.GONE);
            mFabConnect.setVisibility(View.VISIBLE);
            mTextDeviceInfo.setText(errorMsg);
        }else {
            dontListenWifiChanges();
            mFabToggle.setVisibility(View.VISIBLE);
            mFabConnect.setVisibility(View.GONE);
            mTextDeviceInfo.setText(device.toString());
        }
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.fabConnect:
                if(mPresenter!=null) mPresenter.start();
                mFabConnect.setVisibility(View.GONE);
                break;
            case R.id.fabToggle:
                mFabToggle.setVisibility(View.GONE);
                mPresenter.click();
        }
        progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResult(boolean val) {
        if(val)Log.d(TAG, "onResult");
        else Log.e(TAG, "onResult: " );
        Toast.makeText(this, val ? getString(R.string.cmd_success): getString(R.string.cmd_fail), Toast.LENGTH_SHORT).show();
    }
    private void listenWifiChanges(){
        Log.d(TAG, "listenWifiChanges");
        if(receiver==null) receiver= new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String ssid = NetworkChangeReceiver.getConnectedWifiSsid(context);
                Log.d(TAG, "onReceive ssid="+ssid);
                //if(ssid!=null && mPresenter!=null) mPresenter.start();
            }
        };
        registerReceiver(receiver, new IntentFilter(CONNECTIVITY_ACTION));
    }
    private void dontListenWifiChanges(){
        Log.d(TAG, "dontListenWifiChanges");
        if(receiver!=null) {
            unregisterReceiver(receiver);
            receiver=null;
        }
    }
}
