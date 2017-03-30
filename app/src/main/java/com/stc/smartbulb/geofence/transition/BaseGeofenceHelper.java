package com.stc.smartbulb.geofence.transition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.stc.smartbulb.geofence.Constants;
import com.stc.smartbulb.geofence.GeofenceContract;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

/**
 * Base class for {@link GeofenceHelper} implementations.
 */

public abstract class BaseGeofenceHelper implements GeofenceHelper {

    private static final String TAG = "BaseGeofenceHelper";

    protected final Context mContext;
    protected NetworkReceiver mNetworkUpdateReceiver;

    protected GeofenceContract.Presenter mPresenter;

    protected GoogleApiClient mGoogleApiClient;

    public BaseGeofenceHelper(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public void setPresenter(GeofenceContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void start() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(GeofenceTransitionDetector.GEOFENCE_UPDATED);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mGeofenceUpdateReceiver,
                filter);
        registerNetworkReceiver(mContext);
        mGoogleApiClient.connect();
    }

    @Override
    public void stop() {
        unregisterNetworkReceiver(mContext);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mGeofenceUpdateReceiver);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void enableMockLocation() {
        try {
            LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true);
        } catch (SecurityException e) {
            logSecurityException(e);
        }
    }

    @Override
    public void disableMockLocation() {
        try {
            LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, false);
        } catch (SecurityException e) {
            logSecurityException(e);
        }
    }

    @Override
    public void setMockLocation(Location location) {
        try {
            LocationServices.FusedLocationApi.setMockLocation(mGoogleApiClient, location);
        } catch (SecurityException e) {
            logSecurityException(e);
        }
    }

    protected void registerNetworkReceiver(Context context) {
        mNetworkUpdateReceiver = new NetworkReceiver();
        IntentFilter networkFilter = new IntentFilter();
        networkFilter.addAction(CONNECTIVITY_ACTION);
        context.registerReceiver(mNetworkUpdateReceiver, networkFilter);
    }


    private void unregisterNetworkReceiver(Context context) {
        if (mNetworkUpdateReceiver != null) {
            context.unregisterReceiver(mNetworkUpdateReceiver);
            mNetworkUpdateReceiver = null;
        }
    }

    protected abstract void buildGoogleApiClient();

    private class NetworkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (CONNECTIVITY_ACTION.equals(intent.getAction()) && mPresenter.geofenceAdded()) {
                notifyAboutNetworkChange();
            }
        }
    }

    private BroadcastReceiver mGeofenceUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int newGeofenceState = intent.getIntExtra(GeofenceTransitionDetector.
                    KEY_GEOFENCE_UPDATE_TYPE, Constants.GEOFENCE_STATE_UNKNOWN);
            mPresenter.updateGeofenceState(newGeofenceState);
        }
    };


    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid permission.", securityException);
    }

}
