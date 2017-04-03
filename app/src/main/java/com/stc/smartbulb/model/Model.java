package com.stc.smartbulb.model;

import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.stc.smartbulb.R;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;

import static android.content.ContentValues.TAG;

/**
 * Created by artem on 4/3/17.
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class Model extends TileService {

    DeviceSearcher searcher;
    PublishProcessor<Boolean> mProcessor;
    private Device mDevice;

    public Model() {
        super();
        searcher = new DeviceSearcher();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(searcher.isSearching()) searcher.stopSearch();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        processNewState();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        if(mProcessor ==null || mProcessor.hasThrowable()) {
            processNewState();
            if(NetworkChangeReceiver.isMyWifiConnected(this)) {

                searcher.search(new Consumer<Device>() {
                    @Override
                    public void accept(final Device device) throws Exception {
                        mDevice = device;
                        mProcessor = new DeviceController(mDevice).connect();
                        mProcessor.doOnError(new Consumer<Throwable>() {
                            @Override
                            public void accept(final Throwable throwable) throws Exception {
                                mDevice=null;
                                processNewState();
                            }
                        });
                        mProcessor.doOnComplete(new Action() {
                            @Override
                            public void run() throws Exception {
                                processNewState();
                            }
                        });
                    }
                });
            }else Log.w(TAG, "onStartListening: my wifi not connected" );
        } else {
            processNewState();
        }
    }

    public void processNewState(){
        if(mDevice==null)newState(Tile.STATE_UNAVAILABLE);
        else if(mDevice.getState()) newState(Tile.STATE_ACTIVE);
        else newState(Tile.STATE_INACTIVE);
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();
        mProcessor.onNext(!mDevice.getState());
    }
    private Tile newState(int state){
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
