package com.stc.smartbulb.rx2;

import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.stc.smartbulb.R;
import com.stc.smartbulb.model.Device;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class Rx2TestActivity extends AppCompatActivity {
    private static final String TAG = "Rx2TestActivity";
    private TextView mTextDeviceInfo;
    private ProgressBar mProgress;
    private FloatingActionButton mFabToggle;
    private BroadcastReceiver mReceiver;
    private Rx2Presenter mPresenter;
    private CompositeDisposable mDisposables;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx2_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mProgress = (ProgressBar)findViewById(R.id.progress);
        mProgress.setVisibility(View.GONE);
        mFabToggle = (FloatingActionButton) findViewById(R.id.fabToggle);
        mFabToggle.setVisibility(View.VISIBLE);
        mTextDeviceInfo = (TextView) findViewById(R.id.text_device_info);
        mDisposables= new CompositeDisposable();
        mPresenter=new Rx2Presenter();
        newState(null, "click to connect");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mDisposables!=null && !mDisposables.isDisposed()) mDisposables.dispose();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (isVoiceInteractionRoot()) {
            onClick(null);
            finish();
        }

    }
    public void onClick(View v){
        mProgress.setVisibility(View.VISIBLE);
        mFabToggle.setVisibility(View.GONE);
        mDisposables.add(
                mPresenter.sendToggleCmdObservable()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                device -> newState(device, null),
                                throwable -> newState(null, throwable.getMessage())
                        ));
    }

    private void newState(Device device, String  errorMsg) {
        Log.d(TAG, "onUpdateDevice: "+device);
        Log.d(TAG, "onUpdateMsg: "+errorMsg);
        if(device==null) mTextDeviceInfo.setText("error: "+errorMsg);
        else {
            String info= String.format("device %s : %s", device.getIp(), device.isTurnedOn()? "on" : "off");
            mTextDeviceInfo.setText(info);
        }
        mProgress.setVisibility(View.GONE);
        mFabToggle.setVisibility(View.VISIBLE);
    }


}
