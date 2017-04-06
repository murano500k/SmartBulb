package com.stc.smartbulb.qst;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import com.stc.smartbulb.R;
import com.stc.smartbulb.model.Device;
import com.stc.smartbulb.model.NetworkChangeReceiver;
import com.stc.smartbulb.rx2.Rx2BulbContract;
import com.stc.smartbulb.rx2.Rx2Presenter;
import com.stc.smartbulb.trigger.TriggerActivity;

import static com.stc.smartbulb.model.Rx2DeviceManager.CMD_GET_PROP;
import static com.stc.smartbulb.model.Rx2DeviceManager.CMD_TOGGLE;

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
        startActivity(new Intent(this, TriggerActivity.class));
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        Log.d(TAG, "onTileRemoved: ");
    }

    @Override
    public void onStartListening() {
        Log.d(TAG, "onStartListening: ");
        if(!mPresenter.isRunning())newTileState(Tile.STATE_UNAVAILABLE);
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

        mPresenter.sendCmd(cmd, NetworkChangeReceiver.isMyNetworkConnected(this));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onStopListening: "+mPresenter.isRunning());
        mPresenter.cancel();
    }

    @Override
    public void newState(Device device, String msg) {
        Log.d(TAG, "newState: "+msg);
        if(device==null) newTileState(Tile.STATE_UNAVAILABLE);
        else newTileState(device.isTurnedOn() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE );
    }

    @Override
    public void setPresenter(Rx2BulbContract.Presenter presenter) {
        this.mPresenter=presenter;

    }

    private Tile newTileState(int state){
        Tile tile = getQsTile();
        switch (state){
            case Tile.STATE_ACTIVE:
                tile.setIcon(Icon.createWithResource(this,
                        R.drawable.ic_lightbulb_on));
                tile.setLabel(getString(R.string.tile_on));
                tile.setContentDescription(
                        getString(R.string.tile_content_description));
                tile.setState(Tile.STATE_ACTIVE);
                tile.updateTile();
                break;
            case Tile.STATE_INACTIVE:
                tile.setIcon(Icon.createWithResource(this,
                        R.drawable.ic_lightbulb_off));
                tile.setLabel(getString(R.string.tile_off));
                tile.setContentDescription(
                        getString(R.string.tile_content_description));
                tile.setState(Tile.STATE_INACTIVE);
                tile.updateTile();
                break;
            case Tile.STATE_UNAVAILABLE:
            default:
                tile.setIcon(Icon.createWithResource(this,
                        R.drawable.ic_lightbulb_not_available));
                tile.setLabel(getString(R.string.not_available));
                tile.setContentDescription(
                        getString(R.string.tile_content_description));
                tile.setState(Tile.STATE_UNAVAILABLE);
                tile.updateTile();
                break;
        }
       return tile;
    }


}

