package com.stc.smartbulb.ui;

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

import com.stc.smartbulb.R;
import com.stc.smartbulb.model.Connection;
import com.stc.smartbulb.model.Device;
import com.stc.smartbulb.model.NetworkChangeReceiver;
import com.stc.smartbulb.model.RxModel;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class RxTestActivity extends AppCompatActivity {

    private static final String TAG = "RxTestActivity";
    private static final int DEVICE_STATUS_ON = 236;
    private RxModel mRxModel;
    private boolean mDeviceStatus; private boolean mConnectionStatus;
    private static final int CONNECTION_STATUS_NOT_AVAILABLE = 223;
    private static final int CONNECTION_STATUS_CONNECTED = 223;
    private static final int DEVICE_STATUS_OFF = 801;
    private FloatingActionButton mFab;
    private TextView mTextDeviceInfo;
    private TextView mTextDeviceStatus;
    private TextView mTextDeviceConnected;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(NetworkChangeReceiver.isMyWifiConnected(context)) {
                Log.d(TAG, "onReceive: is connected");
                onClick(mFab);
            }
        }
    };
    private Observable<Connection> connectionObservable;
    private ProgressBar progress;
    private Disposable connectionDisposable;
    private PublishSubject<Boolean> connectionSubject;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_test);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        progress = (ProgressBar)findViewById(R.id.progress);
        mFab = (FloatingActionButton) findViewById(R.id.fabConnect);
        mTextDeviceInfo = (TextView) findViewById(R.id.text_device_info);
        mTextDeviceStatus = (TextView) findViewById(R.id.text_device_status);
        mTextDeviceConnected = (TextView) findViewById(R.id.text_device_connected);
        mRxModel = new RxModel();
        setDeviceStatus(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getApplicationContext().registerReceiver(receiver, new IntentFilter(CONNECTIVITY_ACTION));
        if(connectionSubject==null || !connectionSubject.hasObservers()) {
            connectionSubject=mRxModel.connect();
            setDeviceStatus(null);
        }else {

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        getApplicationContext().unregisterReceiver(receiver);
    }


    private void setDeviceStatus(Device device){

        setLoading(false);
        if(device==null) {
            mTextDeviceConnected.setText(getString(R.string.not_connected));
            mTextDeviceInfo.setText("");
            mTextDeviceStatus.setText("");
            mFab.setImageResource(R.drawable.ic_lightbulb_not_available);
            mConnectionStatus=false;
            return;
        }
        mConnectionStatus=true;
        mDeviceStatus=device.isTurnedOn();
        mTextDeviceInfo.setText(device.toString());
        mTextDeviceStatus.setText(device.isTurnedOn()? getString(R.string.bulb_on) : getString(R.string.bulb_off));
        mFab.setImageResource(device.isTurnedOn() ? R.drawable.ic_lightbulb_on : R.drawable.ic_lightbulb_off);
        connectionSubject = mRxModel.connect();

    }

    public void onClick(View v){
        Log.d(TAG, "onClick: ");
        setLoading(true);
        if(connectionDisposable==null || connectionDisposable.isDisposed()) {
            Log.w(TAG, "onClick: no connection. connecting..." );
            connectionObservable.subscribe(deviceInfoConsumer(), connectionErrorConsumer());
            connectionDisposable=connectionObservable.subscribeOn(Schedulers.io()).subscribe(logReadConsumer());
            PublishSubject<Boolean> publishSubject = PublishSubject.create();

            publishSubject.subscribe(new Consumer<Boolean>() {
                @Override
                public void accept(Boolean aBoolean) throws Exception {
                    Connection connection;
                }
            });
        }else {
            Log.w(TAG, "onClick: isnot null" );
            connectionDisposable.dispose();
            connectionDisposable=connectionObservable.subscribe(sendCmdConsumer(!mDeviceStatus), connectionErrorConsumer());
        }
    }
    private Consumer<Connection> logReadConsumer(){
        return new Consumer<Connection>() {
            @Override
            public void accept(Connection connection) throws Exception {
                while (connection.getBis()!=null && connection.getSocket().isConnected() && connection.getBis().ready()) {
                    Log.d(TAG, "msg from device: " +connection.getBis().readLine());
                }

            }
        };
    }

    private Consumer<Connection> sendCmdConsumer(boolean cmd){
        return new Consumer<Connection>() {
            @Override
            public void accept(Connection connection) throws Exception {
                Log.d(TAG, "accept: "+connection);
                if(connection==null || connection.getDevice()==null ||
                        connection.getBos()==null || !connection.getSocket().isConnected()){
                    setDeviceStatus(null);
                }else {
                    setDeviceStatus(connection.getDevice());
                    mRxModel.sendCmd(cmd, connection).subscribe(new Consumer<Device>() {
                        @Override
                        public void accept(Device device) throws Exception {
                            setDeviceStatus(device);
                        }
                    }, connectionErrorConsumer());
                }
            }
        };
    }
    private Consumer<Connection> deviceInfoConsumer(){
        return new Consumer<Connection>() {
            @Override
            public void accept(Connection connection) throws Exception {
                Log.d(TAG, "accept: "+connection);
                if(connection==null || connection.getDevice()==null ||
                        connection.getBos()==null || !connection.getSocket().isConnected()){
                    setDeviceStatus(null);
                }else {
                    setDeviceStatus(connection.getDevice());
                }
            }
        };
    }

    private Consumer<Throwable> connectionErrorConsumer(){
        return new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                Log.e(TAG, "error: "+throwable.getMessage());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        setDeviceStatus(null);
                    }
                });
            }
        };
    }
    private void setLoading(boolean val){
        progress.setVisibility(val ? VISIBLE : GONE);
    }



}
