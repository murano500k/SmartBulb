package com.stc.smartbulb.qst;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import com.stc.smartbulb.R;
import com.stc.smartbulb.model.Device;
import com.stc.smartbulb.rx2.Rx2Presenter;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

@TargetApi(Build.VERSION_CODES.N)
public class QstService extends TileService {
    private Rx2Presenter mPresenter;
    private static final String TAG = "QstService";
    private BroadcastReceiver mReceiver;

    private CompositeDisposable mDisposables;
    public QstService() {
        super();
        mPresenter= new Rx2Presenter();
        mDisposables = new CompositeDisposable();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        Log.d(TAG, "onTileAdded: ");
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
        Log.d(TAG, "onTileRemoved: ");
    }

    @Override
    public void onStartListening() {
        Log.d(TAG, "onStartListening: ");
        if(mDisposables.isDisposed())newTileState(Tile.STATE_UNAVAILABLE);
        if(getQsTile().getState()==Tile.STATE_UNAVAILABLE)onClick();
       /* mDisposables.add(
                mPresenter.getStateObservable()
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                device -> newState(device, null),
                                throwable -> newState(null, throwable.getMessage())
                        ));
*/
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        Log.d(TAG, "onStopListening: "+mDisposables.isDisposed());
        if(!mDisposables.isDisposed()) mDisposables.dispose();

    }

    @Override
    public void onClick() {
        super.onClick();
        Log.d(TAG, "onClick: "+getQsTile().getState());
        switch (getQsTile().getState()){
            case Tile.STATE_UNAVAILABLE:
                subscribe(mPresenter.getStateObservable());
                break;
            case Tile.STATE_ACTIVE:
                //subscribe(mPresenter.sendPowerCmdObservable(false));
                subscribe(mPresenter.sendToggleCmdObservable());
                break;
            case Tile.STATE_INACTIVE:
                //subscribe(mPresenter.sendPowerCmdObservable(true));
                subscribe(mPresenter.sendToggleCmdObservable());
                break;

        }
    }

    private void subscribe(Observable<Device> observable){
        mDisposables.add(
                observable
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                device -> newState(device, null),
                                throwable -> newState(null, throwable.getMessage()),
                                () -> {
                                    newTileState(Tile.STATE_UNAVAILABLE);
                                }
                        ));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: "+mDisposables.isDisposed());

    }


    private void newState(Device device, String msg) {
        Log.d(TAG, "newState: "+msg);
        if(device==null) newTileState(Tile.STATE_UNAVAILABLE);
        else newTileState(device.isTurnedOn() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE );
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
                tile.setLabel(getString(R.string.tile_not_available));
                tile.setContentDescription(
                        getString(R.string.tile_content_description));
                tile.setState(Tile.STATE_UNAVAILABLE);
                tile.updateTile();
                break;
        }
       return tile;
    }


}

