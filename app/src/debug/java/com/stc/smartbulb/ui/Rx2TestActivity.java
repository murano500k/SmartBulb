package com.stc.smartbulb.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.stc.smartbulb.Device;
import com.stc.smartbulb.R;
import com.stc.smartbulb.Rx2BulbContract;
import com.stc.smartbulb.Rx2DeviceManager;
import com.stc.smartbulb.Rx2Presenter;
import com.stc.smartbulb.model.NetworkChangeReceiver;
import com.stc.smartbulb.trigger.TriggerActivity;

public class Rx2TestActivity extends AppCompatActivity implements Rx2BulbContract.View {
    private static final String TAG = "Rx2TestActivity";
    private TextView mTextDeviceInfo;
    private ProgressBar mProgress;
    private FloatingActionButton mFabToggle;
    private Rx2BulbContract.Presenter mPresenter;
    private ImageView mImageBulb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx2_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mImageBulb=(ImageView)findViewById(R.id.image_status);
        mImageBulb.setImageResource(R.drawable.ic_lightbulb_not_available);
        mProgress = (ProgressBar)findViewById(R.id.progress);
        mProgress.setVisibility(View.GONE);
        mFabToggle = (FloatingActionButton) findViewById(R.id.fabToggle);
        mFabToggle.setVisibility(View.VISIBLE);
        mTextDeviceInfo = (TextView) findViewById(R.id.text_device_info);
        new Rx2Presenter(this);
        newState(null, "click to connect");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.action_settings) {
            startActivity(new Intent(this, TriggerActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.cancel();
    }

    public void onClick(View v){
        mProgress.setVisibility(View.VISIBLE);
        mFabToggle.setVisibility(View.GONE);
        mPresenter.sendCmd(Rx2DeviceManager.CMD_TOGGLE, NetworkChangeReceiver.isMyNetworkConnected(this));
    }
    @Override
    public void newState(Device device, String errorMsg) {
        if(device==null) {
            Log.e(TAG, "onUpdate msg: "+errorMsg);
            mTextDeviceInfo.setText(errorMsg);
            mImageBulb.setImageResource(R.drawable.ic_lightbulb_not_available);
        }
        else {
            String info= String.format("device %s : %s", device.getIp(), device.isTurnedOn()? "on" : "off");
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
    }


}
