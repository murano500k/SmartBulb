package com.stc.smartbulb.trigger;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.stc.smartbulb.Device;
import com.stc.smartbulb.Rx2BulbContract;
import com.stc.smartbulb.Rx2Presenter;
import com.stc.smartbulb.model.NetworkChangeReceiver;

public class TriggerAlarmService extends IntentService implements Rx2BulbContract.View {
    private static final String TAG = TriggerAlarmService.class.getSimpleName();

    private static final String  ACTION_SWITCH_BULB = "ACTION_SWITCH_BULB";
    private static final String EXTRA_WIDGET_ID = "EXTRA_WIDGET_ID";
    private static final String  EXTRA_CMD = "EXTRA_CMD";
    public static final int TRIGGER_SERVICE_ID = 3645;
    private Rx2BulbContract.Presenter mPresenter;
    private int mWidgetId;

    public TriggerAlarmService() {
        super(TAG);
        new Rx2Presenter(this);
        mWidgetId=Integer.MAX_VALUE;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent!=null && intent.getStringExtra(EXTRA_CMD)!=null) {
            String cmd = intent.getStringExtra(EXTRA_CMD);
            mWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID , Integer.MAX_VALUE);
            mPresenter.sendCmd(cmd, NetworkChangeReceiver.isMyNetworkConnected(this));
        }else Log.e(TAG, "onHandleIntent: no action" );
    }

    @Override
    public void newState(Device device, String msg) {
        if(device!=null){
            Log.d(TAG, "newState: " + device);
            if(mWidgetId!=Integer.MAX_VALUE) {
                Log.d(TAG, "newState: will update widget");
            }
        }else {
            Log.e(TAG, "newState: "+msg );
        }
    }

    @Override
    public void setPresenter(Rx2BulbContract.Presenter presenter) {
        this.mPresenter=presenter;
    }

    private static Intent getIntent(Context context, String cmd){
        Intent intent = new Intent(context, TriggerAlarmService.class);
        intent.setAction(ACTION_SWITCH_BULB);
        intent.putExtra(EXTRA_CMD, cmd);
        return intent;
    }

    public static PendingIntent getPendingIntent(Context context, String cmd) {
        return PendingIntent.getService(context, TRIGGER_SERVICE_ID,
                getIntent(context,cmd),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }


}
