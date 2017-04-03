package com.stc.smartbulb.controller;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.stc.smartbulb.model.Device;

import static com.google.android.gms.wearable.DataMap.TAG;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class BulbSwitchService extends IntentService implements ControllerCallback {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_SWITCH_BULB = "com.stc.smartbulb.action.ACTION_SWITCH_BULB";

    // TODO: Rename parameters
    private static final String EXTRA_VAL = "com.stc.smartbulb.extra.PARAM1";
    private BulbController controller;

    public BulbSwitchService() {
        super("BulbSwitchService");
        /*controller=new BulbController(PrefsUtils.getSavedBulbIP(getApplicationContext()),
                PrefsUtils.getSavedBulbPort(getApplicationContext()),
                this);*/

    }

    public static void startActionFoo(Context context, boolean val) {
        Intent intent = new Intent(context, BulbSwitchService.class);
        intent.setAction(ACTION_SWITCH_BULB);
        intent.putExtra(EXTRA_VAL, val);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SWITCH_BULB.equals(action)) {
                final boolean param1 = intent.getBooleanExtra(EXTRA_VAL, false);
                handleActionFoo(param1);
            }
        }
    }
    private void handleActionFoo(boolean param1) {
        controller.switchBulb(param1);
    }

    public void onResult(boolean result, boolean val) {
        Log.d(TAG, "onResult: "+result);
        Log.d(TAG, "onVal: "+val);
    }

    @Override
    public void deviceFound(final Device device) {

    }

    @Override
    public void deviceNotFound() {

    }

    @Override
    public void deviceConnectedSuccessfully(final Device device) {

    }

    @Override
    public void deviceConnectFailed(final Device device) {

    }

    @Override
    public void setController(final ControllerInterface controller) {

    }
}
