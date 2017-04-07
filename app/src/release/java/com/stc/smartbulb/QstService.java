package com.stc.smartbulb;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.stc.smartbulb.ui.Rx2TestActivity;

import static com.stc.smartbulb.Rx2DeviceManager.CMD_GET_PROP;
import static com.stc.smartbulb.Rx2DeviceManager.CMD_TOGGLE;

@TargetApi(Build.VERSION_CODES.N)
public class QstService extends TileService implements Rx2BulbContract.View {
    private Rx2BulbContract.Presenter mPresenter;
    private static final String TAG = "QstService";

    public QstService() {
        super();
        new Rx2Presenter(this);

    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        Log.d(TAG, "onTileAdded: ");
        logEvent(FirebaseAnalytics.Event.APP_OPEN, "tile added");
        startActivity(new Intent(this, Rx2TestActivity.class));
    }



    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        Log.d(TAG, "onTileRemoved: ");
        logEvent(FirebaseAnalytics.Event.POST_SCORE, "tile removed");
    }

    @Override
    public void onStartListening() {
        Log.d(TAG, "onStartListening: ");

        if(!mPresenter.isRunning())newTileState(Tile.STATE_UNAVAILABLE, "Searching...");
        if(getQsTile().getState()==Tile.STATE_UNAVAILABLE)onClick();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        Log.d(TAG, "onStopListening: "+mPresenter.isRunning());
        mPresenter.cancel();
    }

    @Override
    public void onClick() {
        super.onClick();
        logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, "tile clicked");
        String cmd;
        int state=getQsTile().getState();
        Log.d(TAG, "onClick: "+state);

        switch (state){
            case Tile.STATE_ACTIVE:
                cmd=CMD_TOGGLE;
                break;
            case Tile.STATE_INACTIVE:
                cmd=CMD_TOGGLE;
                break;
            case Tile.STATE_UNAVAILABLE:
            default:
                cmd=CMD_GET_PROP;
                break;
        }

        mPresenter.sendCmd(cmd, NetworkChangeReceiver.isWifiConnected(this));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onStopListening: "+mPresenter.isRunning());
        mPresenter.cancel();
    }

    @Override
    public void newState(Device device, String msg) {
        Log.d(TAG, "newState: msg="+msg);
        Log.d(TAG, "newState: device="+device);
        if(device==null) newTileState(Tile.STATE_UNAVAILABLE, msg);
        else {
            String info= String.format("%s %s", device.getName(), device.isTurnedOn()? "on" : "off");
            newTileState(device.isTurnedOn() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE ,info);
        }
    }

    @Override
    public void setPresenter(Rx2BulbContract.Presenter presenter) {
        this.mPresenter=presenter;

    }

    private Tile newTileState(int state, String msg){
        Tile tile = getQsTile();
        switch (state){
            case Tile.STATE_ACTIVE:
                tile.setIcon(Icon.createWithResource(this,
                        R.drawable.ic_lightbulb_on));
                tile.setLabel(msg);
                tile.setContentDescription(
                        getString(R.string.tile_content_description));
                tile.setState(Tile.STATE_ACTIVE);
                tile.updateTile();
                break;
            case Tile.STATE_INACTIVE:
                tile.setIcon(Icon.createWithResource(this,
                        R.drawable.ic_lightbulb_off));
                tile.setLabel(msg);
                tile.setContentDescription(
                        getString(R.string.tile_content_description));
                tile.setState(Tile.STATE_INACTIVE);
                tile.updateTile();
                break;
            case Tile.STATE_UNAVAILABLE:
            default:
                tile.setIcon(Icon.createWithResource(this,
                        R.drawable.ic_lightbulb_not_available));
                tile.setLabel(msg);
                tile.setContentDescription(
                        getString(R.string.tile_content_description));
                tile.setState(Tile.STATE_UNAVAILABLE);
                tile.updateTile();
                break;
        }
       return tile;
    }
    private void logEvent(String postScore, String msg){
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.VALUE, msg);
        FirebaseAnalytics.getInstance(getApplicationContext()).logEvent(postScore, bundle);
    }

}

