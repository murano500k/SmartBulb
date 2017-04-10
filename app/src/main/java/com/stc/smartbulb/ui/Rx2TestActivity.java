package com.stc.smartbulb.ui;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.stc.smartbulb.Device;
import com.stc.smartbulb.PrefsUtils;
import com.stc.smartbulb.R;
import com.stc.smartbulb.Rx2BulbContract;
import com.stc.smartbulb.Rx2DeviceManager;
import com.stc.smartbulb.Rx2Presenter;
import com.stc.smartbulb.model.NetworkChangeReceiver;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

public class Rx2TestActivity extends AppCompatActivity implements Rx2BulbContract.View {
    private static final String TAG = "Rx2TestActivity";
    private TextView mTextDeviceInfo, mTextWifi;
    private ProgressBar mProgress;
    private FloatingActionButton mFabToggle;
    private Rx2BulbContract.Presenter mPresenter;
    private ImageView mImageBulb;
    private ImageButton mBtnParentApp;
    private NetworkChangeReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx2_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.qst_dialog_title);
        mImageBulb=(ImageView)findViewById(R.id.image_status);
        mImageBulb.setImageResource(R.drawable.ic_lightbulb_not_available);
        mProgress = (ProgressBar)findViewById(R.id.progress);
        mProgress.setVisibility(View.GONE);
        mFabToggle = (FloatingActionButton) findViewById(R.id.fabToggle);
        mFabToggle.setVisibility(View.VISIBLE);
        mTextDeviceInfo = (TextView) findViewById(R.id.text_device_info);
        mTextWifi = (TextView) findViewById(R.id.text_wifi);
        mBtnParentApp = (ImageButton) findViewById(R.id.btn_parent_app);
        Picasso.with(this).load(getString(R.string.parent_app_icon_url)).fit().into(mBtnParentApp);
        new Rx2Presenter(this);
        newState(null, getString(R.string.click_to_test));
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkWifi();
    }
    private void checkWifi(){
        String wifi = NetworkChangeReceiver.getConnectedWifiSsid(this);
        if(wifi==null) {
            wifi=getString(R.string.wifi_not_connected);
        }else {
            PrefsUtils.saveWifiSSID(this, wifi);
        }
        mTextWifi.setText(wifi);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.cancel();
        unregisterReceiver(mReceiver);
    }

    public void onClick(View v){
        if(!mReceiver.getState()) {
            newState(null, "Wifi not set");
            mPresenter.cancel();
            return;
        }
        mProgress.setVisibility(View.VISIBLE);
        mFabToggle.setVisibility(View.GONE);
        mPresenter.sendCmd(Rx2DeviceManager.CMD_TOGGLE);
    }
    @Override
    public void newState(Device device, String errorMsg) {
        checkWifi();
        if(device==null) {
            Log.e(TAG, "onUpdate msg: "+errorMsg);
            mTextDeviceInfo.setText(errorMsg);
            mImageBulb.setImageResource(R.drawable.ic_lightbulb_not_available);
        }
        else {
            String info= String.format("%s %s : %s", device.getName(), device.getIp(), device.isTurnedOn()? "on" : "off");
            Log.d(TAG, "onUpdate Device: "+info);
            mImageBulb.setImageResource(device.isTurnedOn() ? R.drawable.ic_lightbulb_on : R.drawable.ic_lightbulb_off);
            mTextDeviceInfo.setText(info);
        }
        mProgress.setVisibility(View.GONE);
        mFabToggle.setVisibility(View.VISIBLE);
    }

    @Override
    public void setPresenter(Rx2BulbContract.Presenter presenter) {
        this.mPresenter=presenter;
        mReceiver=new NetworkChangeReceiver(this, this, mPresenter);
        registerReceiver(mReceiver, new IntentFilter(CONNECTIVITY_ACTION));
    }

    public void onParentAppBtnClick(View v){
        Uri parentAppUri = Uri.parse(
                getString(R.string.store_base_url) +
                        getString(R.string.parent_app_package));
        getApplicationContext().startActivity(new Intent().setAction(Intent.ACTION_VIEW).setData(parentAppUri));
    }



}
