package com.stc.smartbulb.qst;

import android.annotation.TargetApi;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;

import com.stc.smartbulb.R;
import com.stc.smartbulb.model.Device;
import com.stc.smartbulb.rx2.Rx2Contract;
import com.stc.smartbulb.rx2.Rx2Presenter;

@TargetApi(Build.VERSION_CODES.N)
public class QstService extends TileService implements Rx2Contract.View{
    private Rx2Contract.Presenter mPresenter;
    private static final String TAG = "QstService";
    public QstService() {
        super();
        new Rx2Presenter(this);
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
        mPresenter.start();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        Log.d(TAG, "onStopListening: ");
        if(mPresenter!=null)mPresenter.finish();
    }
    @Override
    public void onClick() {
        Log.d(TAG, "onClick: ");
        if(mPresenter!=null) mPresenter.click();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
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

    @Override
    public void setPresenter(Rx2Contract.Presenter presenter) {
        this.mPresenter = presenter;
    }

    @Override
    public void onUpdate(Device device, String errorMsg) {
        if(device==null) newState(Tile.STATE_UNAVAILABLE);
        else newState(device.isTurnedOn() ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
    }

    @Override
    public void onResult(boolean val) {
        if(val)Log.d(TAG, "onResult");
        else Log.e(TAG, "onResult: " );
    }

    private void showDialog(){
        com.google.android_quick_settings.QSDialog.Builder dialogBuilder =
                new com.google.android_quick_settings.QSDialog.Builder(getApplicationContext());

        com.google.android_quick_settings.QSDialog dialog = dialogBuilder
                .setClickListener(new com.google.android_quick_settings.QSDialog.QSDialogListener() {

                    @Override
                    public void onDialogPositiveClick(DialogFragment dialog) {
                        Log.d("QS", "Positive registed");

                        // The user wants to change the tile state.
                        isTileActive = !isTileActive;
                        updateTile();
                    }

                    @Override
                    public void onDialogNegativeClick(DialogFragment dialog) {
                        Log.d("QS", "Negative registered");

                        // The user is cancelled the dialog box.
                        // We can't do anything to the dialog box here,
                        // but we can do any cleanup work.
                    }
                })
                .create();

        // Pass the tile's current state to the dialog.
        Bundle args = new Bundle();
        args.putBoolean(com.google.android_quick_settings.QSDialog.TILE_STATE_KEY, isTileActive);

        this.showDialog(dialog.onCreateDialog(args));
    }
}

