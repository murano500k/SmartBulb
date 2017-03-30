package com.stc.smartbulb.geofence.transition;

import android.app.IntentService;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import com.ashaevy.geofence.Injection;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.stc.smartbulb.R;
import com.stc.smartbulb.geofence.data.source.GeofenceDataSource;

/**
 * Listener for geofence transition changes.
 *
 * Receives geofence transition events from Location Services.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "GeofenceTransitionsIS";
    private GeofenceDataSource mDataSource;
    private GeofenceTransitionDetector mGeofenceTransitionDetector;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public GeofenceTransitionsIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDataSource = Injection.provideGeofenceDataSource(this);
        mGeofenceTransitionDetector = new GeofenceTransitionDetector(mDataSource);
    }

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // Handle network change
        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
            mGeofenceTransitionDetector.detectTransition(this);
            return;
        }

        // Handle geofence transition change
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            mDataSource.saveGeofenceTransition(geofenceTransition);
            mGeofenceTransitionDetector.detectTransition(this);

            Log.i(TAG, getTransitionString(geofenceTransition));
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }

}
