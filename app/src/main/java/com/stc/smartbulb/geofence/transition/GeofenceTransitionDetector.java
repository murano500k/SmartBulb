package com.stc.smartbulb.geofence.transition;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.google.android.gms.location.Geofence;
import com.stc.smartbulb.geofence.Constants;
import com.stc.smartbulb.geofence.data.GeofenceData;
import com.stc.smartbulb.geofence.data.source.GeofenceDataSource;
import com.stc.smartbulb.utils.NetworkUtils;

/**
 * Class that contains logic of detection Geofence transitions.
 */

public class GeofenceTransitionDetector {

    public static final String GEOFENCE_UPDATED = GeofenceTransitionsIntentService.class.getName() + ".GEOFENCE_UPDATED";
    public static final String KEY_GEOFENCE_UPDATE_TYPE = "KEY_GEOFENCE_UPDATE_TYPE";

    private GeofenceDataSource mGeofenceDataSource;

    public GeofenceTransitionDetector(GeofenceDataSource geofenceDataSource) {
        mGeofenceDataSource = geofenceDataSource;
    }

    public void detectTransition(final Context context) {
        detectTransition(context, new SsidProvider() {
            @Override
            public String getSsid() {
                return NetworkUtils.getCurrentSsid(context);
            }
        });
    }

    void detectTransition(Context context, SsidProvider ssidProvider) {
        Intent updateIntent = new Intent(GEOFENCE_UPDATED);
        updateIntent.putExtra(KEY_GEOFENCE_UPDATE_TYPE, detectTransitionState(ssidProvider));
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent);
    }

    int detectTransitionState(SsidProvider ssidProvider) {
        int geofenceState = Constants.GEOFENCE_STATE_UNKNOWN;
        int geofenceTransition = mGeofenceDataSource.readGeofenceTransition();

        GeofenceData geofenceData = mGeofenceDataSource.readGeofenceData();
        if (geofenceData != null) {
            String wifiName = geofenceData.getWifiName();
            String currentSsid = ssidProvider.getSsid();
            if ((!TextUtils.isEmpty(wifiName) && wifiName.equals(currentSsid)) ||
                    (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)) {
                geofenceState = Constants.GEOFENCE_STATE_INSIDE;
            } else {
                geofenceState = Constants.GEOFENCE_STATE_OUTSIDE;
            }
        }
        return geofenceState;
    }

    /**
     * Provides last possible transition based on location and geofenceData.
     *
     * @return Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
     */
    int geofenceCoordinatesTransition(GeofenceData geofenceData, Location location) {
        float[] results = new float[1];
        Location.distanceBetween(geofenceData.getLatitude(), geofenceData.getLongitude(),
                location.getLatitude(), location.getLongitude(), results);
        float distanceInMeters = results[0];
        if (distanceInMeters <= geofenceData.getRadius()) {
            return Geofence.GEOFENCE_TRANSITION_ENTER;
        } else {
            return Geofence.GEOFENCE_TRANSITION_EXIT;
        }
    }

    interface SsidProvider {
        String getSsid();
    }

}
