package com.stc.smartbulb.geofence.data.source;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.stc.smartbulb.geofence.Constants;
import com.stc.smartbulb.geofence.data.GeofenceData;

/**
 * Data source that store geofence data in Shared Preferences using GSON.
 */
public class SPGeofenceDataSource implements GeofenceDataSource {

    private static SPGeofenceDataSource instance;

    private Gson gson = new Gson();
    private SharedPreferences mSharedPreferences;

    private SPGeofenceDataSource(Context context) {
        mSharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
    }

    @Override
    public void saveGeofenceData(GeofenceData geofenceData) {
        String geofenceDataString = gson.toJson(geofenceData);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Constants.GEOFENCE_DATA_KEY, geofenceDataString);
        editor.apply();
    }

    @Override
    public GeofenceData readGeofenceData() {
        String json = mSharedPreferences.getString(Constants.GEOFENCE_DATA_KEY, null);
        if (json == null) {
            return generateDefaultGeofence();
        }
        return gson.fromJson(json, GeofenceData.class);
    }

    public static GeofenceData generateDefaultGeofence() {
        GeofenceData result = new GeofenceData();
        result.setLatitude(Constants.KIEV.latitude);
        result.setLongitude(Constants.KIEV.longitude);
        result.setRadius(Constants.DEFAULT_RADIUS);
        result.setWifiName(Constants.DEFAULT_WIFI);
        return result;
    }

    @Override
    public boolean geofenceAdded() {
        return mSharedPreferences.getBoolean(Constants.GEOFENCES_ADDED_KEY, false);
    }

    @Override
    public void saveGeofenceAdded(boolean geofencesAdded) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(Constants.GEOFENCES_ADDED_KEY, geofencesAdded);
        editor.apply();
    }

    @Override
    public void saveGeofenceTransition(int transition) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(Constants.GEOFENCE_TRANSITION_KEY, transition);
        editor.apply();
    }

    @Override
    public int readGeofenceTransition() {
        return mSharedPreferences.getInt(Constants.GEOFENCE_TRANSITION_KEY, -1);
    }

    public static GeofenceDataSource getInstance(Context context) {
        if (instance == null) {
            instance = new SPGeofenceDataSource(context.getApplicationContext());
        }
        return instance;
    }
}
