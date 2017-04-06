package com.stc.smartbulb.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.widget.RemoteViews;

import com.stc.smartbulb.R;
import com.stc.smartbulb.model.Device;
import com.stc.smartbulb.model.NetworkChangeReceiver;
import com.stc.smartbulb.model.Rx2DeviceManager;
import com.stc.smartbulb.rx2.Rx2BulbContract;
import com.stc.smartbulb.rx2.Rx2Presenter;
import com.stc.smartbulb.trigger.TriggerAlarmService;


/**
 * Created by artem on 4/6/17.
 */
public class WidgetUpdateService extends IntentService implements Rx2BulbContract.View {
    private static final String TAG = WidgetUpdateService.class.getSimpleName();

    public static final String EXTRA_WIDGET_IDS = "widget_ids";
    private static final int REQUEST_WIDGET_UPDATE = 4364;
    private RemoteViews mRemoteViews;
    private Rx2BulbContract.Presenter mPresenter;
    AppWidgetManager mAppWidgetManager;
    public WidgetUpdateService() {
        super(TAG);
        new Rx2Presenter(this);
        mAppWidgetManager = AppWidgetManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mPresenter.sendCmd(Rx2DeviceManager.CMD_GET_PROP, NetworkChangeReceiver.isMyNetworkConnected(this));
    }
    @Override
    public void newState(Device device, String msg){
        mRemoteViews = new RemoteViews(getPackageName(), R.layout.bulb_switch_widget);
        mRemoteViews.setOnClickPendingIntent(mRemoteViews.getLayoutId(), TriggerAlarmService.getPendingIntent(this, Rx2DeviceManager.CMD_TOGGLE));

        if(device==null){
            mRemoteViews.setTextViewText(R.id.appwidget_text, msg);
            mRemoteViews.setImageViewResource(R.id.appwidget_image, R.drawable.ic_lightbulb_not_available);
        }else if(device.isTurnedOn()){
            mRemoteViews.setTextViewText(R.id.appwidget_text, "On");
            mRemoteViews.setImageViewResource(R.id.appwidget_image, R.drawable.ic_lightbulb_on);
        }else {
            mRemoteViews.setTextViewText(R.id.appwidget_text, "On");
            mRemoteViews.setImageViewResource(R.id.appwidget_image, R.drawable.ic_lightbulb_off);
        }

        int[] appWidgetIds = mAppWidgetManager.getAppWidgetIds(new ComponentName(this,
                BulbSwitchWidget.class));

        for (int id : appWidgetIds) {
            mAppWidgetManager.updateAppWidget(id, mRemoteViews);
        }
    }

    @Override
    public void setPresenter(Rx2BulbContract.Presenter presenter) {
        this.mPresenter=presenter;
    }
}