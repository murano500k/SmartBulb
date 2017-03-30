package com.stc.smartbulb.geofence;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.stc.smartbulb.geofence.data.GeofenceData;
import com.stc.smartbulb.geofence.data.source.GeofenceDataSource;
import com.stc.smartbulb.geofence.transition.GeofenceHelper;
import com.stc.smartbulb.utils.NetworkUtils;

import java.util.Date;

/**
 * Synchronize Map and Controls. Listen to geofense state changes. Updates Views.
 */
public class GeofencePresenter implements GeofenceContract.Presenter {

    private final GeofenceHelper mGeofenceHelper;
    private final GeofenceDataSource mGeofenceDataSource;

    private final GeofenceContract.MapView mMapView;
    private final GeofenceContract.ControlsView mControlsView;
    private final GeofenceContract.DialogsView mDialogsView;

    private GeofenceData mCurrentGeofenceData;
    private int mCurrentGeofenceState;
    private boolean mGeofenceAdded;
    private boolean useMockLocation;

    private String KEY_LAT = "KEY_LAT";
    private String KEY_LON = "KEY_LON";
    private String KEY_R = "KEY_R";
    private String KEY_WIFI = "KEY_WIFI";
    private String KEY_GEOFENCE_STATE = "KEY_GEOFENCE_STATE";
    private static final String KEY_GEOFENCE_ADDED = "KEY_GEOFENCE_ADDED";

    public GeofencePresenter(GeofenceDataSource geofenceDataSource,
                             Views views,
                             GeofenceHelper geofenceHelper) {

        mGeofenceDataSource = geofenceDataSource;

        mMapView = views.getMapView();
        mControlsView = views.getControlsView();
        mDialogsView = views.getDialogsView();

        mMapView.setPresenter(this);
        mControlsView.setPresenter(this);

        mGeofenceHelper = geofenceHelper;
        mGeofenceHelper.setPresenter(this);
    }

    public void create(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentGeofenceData = new GeofenceData();
            mCurrentGeofenceData.setLatitude(savedInstanceState.getDouble(KEY_LAT));
            mCurrentGeofenceData.setLongitude(savedInstanceState.getDouble(KEY_LON));
            mCurrentGeofenceData.setRadius(savedInstanceState.getDouble(KEY_R));
            mCurrentGeofenceData.setWifiName(savedInstanceState.getString(KEY_WIFI));
            mCurrentGeofenceState = savedInstanceState.getInt(KEY_GEOFENCE_STATE);
            mGeofenceAdded = savedInstanceState.getBoolean(KEY_GEOFENCE_ADDED);
        } else {
            mCurrentGeofenceData = mGeofenceDataSource.readGeofenceData();
            mCurrentGeofenceState = Constants.GEOFENCE_STATE_UNKNOWN;
            mGeofenceAdded = mGeofenceDataSource.geofenceAdded();
        }

        mGeofenceHelper.create(savedInstanceState);
    }

    @Override
    public void start(Context context) {
        updateGeofenceAddedUIState(mGeofenceAdded);
        mControlsView.updateGeofence(mCurrentGeofenceData);
        mMapView.updateGeofence(mCurrentGeofenceData);

        mGeofenceHelper.start();
    }


    @Override
    public void stop(Context context) {
        mGeofenceHelper.stop();
    }

    @Override
    public void updateGeofenceFromMap(GeofenceData geofenceData) {
        mCurrentGeofenceData.setLatitude(geofenceData.getLatitude());
        mCurrentGeofenceData.setLongitude(geofenceData.getLongitude());
        mCurrentGeofenceData.setRadius(geofenceData.getRadius());

        mControlsView.updateGeofence(mCurrentGeofenceData);
    }

    @Override
    public void startGeofencing() {
        if (useMockLocation) {
            mGeofenceHelper.enableMockLocation();
        }
        mGeofenceDataSource.saveGeofenceData(mCurrentGeofenceData);

        LatLng position = new LatLng(mCurrentGeofenceData.getLatitude(),
                mCurrentGeofenceData.getLongitude());
        mGeofenceHelper.addGeofence(position, mCurrentGeofenceData.getRadius());
    }

    private void updateGeofenceAddedUIState(boolean added) {
        mControlsView.setGeofencingStarted(added);
        mMapView.setGeofencingStarted(added);
    }

    @Override
    public void stopGeofencing() {
        if (useMockLocation) {
            mGeofenceHelper.disableMockLocation();
        }

        mGeofenceHelper.removeGeofence();
        mCurrentGeofenceState = Constants.GEOFENCE_STATE_UNKNOWN;
        mControlsView.setGeofenceState(Constants.GEOFENCE_STATE_UNKNOWN);
    }

    @Override
    public void setRandomMockLocation() {
        Location mockLocation = generateRandomTestLocation();
        mGeofenceHelper.setMockLocation(mockLocation);
        mMapView.setMockLocation(mockLocation);
    }

    public void setUseMockLocation(boolean useMockLocation) {
        this.useMockLocation = useMockLocation;
    }

    @Override
    public void setCurrentWiFi(Context context) {
        String currentSsid = NetworkUtils.getCurrentSsid(context);
        if (currentSsid != null) {
            mCurrentGeofenceData.setWifiName(currentSsid);
            mControlsView.updateGeofence(mCurrentGeofenceData);
        }
    }

    @Override
    public void updateGeofenceFromControls(GeofenceData geofenceData) {
        mCurrentGeofenceData.setLatitude(geofenceData.getLatitude());
        mCurrentGeofenceData.setLongitude(geofenceData.getLongitude());
        mCurrentGeofenceData.setRadius(geofenceData.getRadius());
        mCurrentGeofenceData.setWifiName(geofenceData.getWifiName());

        mMapView.updateGeofence(mCurrentGeofenceData);
    }

    @Override
    public GeofenceData getGeofenceData() {
        return mCurrentGeofenceData;
    }

    @Override
    public int getCurrentGeofenceState() {
        return mCurrentGeofenceState;
    }

    private Location generateRandomTestLocation() {
        Location location = new Location(LocationManager.NETWORK_PROVIDER);
        location.setLatitude(Constants.KIEV.latitude + Math.random() * 0.1);
        location.setLongitude(Constants.KIEV.longitude + Math.random() * 0.1);
        location.setTime(new Date().getTime());
        location.setAccuracy(3.0f);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            location.setElapsedRealtimeNanos(System.nanoTime());
        }

        return location;
    }

    @Override
    public void updateGeofenceState(int newGeofenceState) {
        if (mGeofenceAdded) {
            mCurrentGeofenceState = newGeofenceState;
            mControlsView.setGeofenceState(mCurrentGeofenceState);
        }
    }

    @Override
    public void saveInstanceState(Bundle outState) {
        outState.putDouble(KEY_LAT, mCurrentGeofenceData.getLatitude());
        outState.putDouble(KEY_LON, mCurrentGeofenceData.getLongitude());
        outState.putDouble(KEY_R, mCurrentGeofenceData.getRadius());
        outState.putString(KEY_WIFI, mCurrentGeofenceData.getWifiName());

        outState.putBoolean(KEY_GEOFENCE_ADDED, mGeofenceAdded);
        outState.putInt(KEY_GEOFENCE_STATE, mCurrentGeofenceState);

        mGeofenceHelper.saveInstanceState(outState);
    }

    @Override
    public void updateGeofenceAddedState(boolean geofenceAdded) {
        mGeofenceAdded = geofenceAdded;
        updateGeofenceAddedUIState(geofenceAdded);
        mGeofenceDataSource.saveGeofenceAdded(mGeofenceAdded);
    }

    @Override
    public boolean geofenceAdded() {
        return mGeofenceAdded;
    }

    @Override
    public void reportPermissionError(int requestId) {
        updateGeofenceAddedState(false);
        mDialogsView.requestLocationPermission(requestId);
    }

    @Override
    public void reportNotReadyError() {
        mDialogsView.reportNotReadyError();
    }

    @Override
    public void reportErrorMessage(String errorMessage) {
        mDialogsView.reportErrorMessage(errorMessage);
    }

    public static class Views {
        private final GeofenceContract.MapView mMapView;
        private final GeofenceContract.ControlsView mControlsView;
        private final GeofenceContract.DialogsView mDialogsView;

        public Views(GeofenceContract.MapView mMapView, GeofenceContract.ControlsView mControlsView,
                     GeofenceContract.DialogsView mDialogsView) {
            this.mMapView = mMapView;
            this.mControlsView = mControlsView;
            this.mDialogsView = mDialogsView;
        }

        public GeofenceContract.MapView getMapView() {
            return mMapView;
        }

        public GeofenceContract.ControlsView getControlsView() {
            return mControlsView;
        }

        public GeofenceContract.DialogsView getDialogsView() {
            return mDialogsView;
        }
    }

}
