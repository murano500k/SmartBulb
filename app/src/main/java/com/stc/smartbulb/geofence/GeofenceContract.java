package com.stc.smartbulb.geofence;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.stc.smartbulb.geofence.data.GeofenceData;


/**
 * Contract between Views and Presenters.
 */
public interface GeofenceContract {
    interface MapView {

        void setPresenter(Presenter presenter);

        void updateGeofence(GeofenceData geofenceData);

        void setMockLocation(Location location);

        void setGeofencingStarted(boolean started);
    }

    interface ControlsView {

        void setPresenter(Presenter presenter);

        void updateGeofence(GeofenceData geofenceData);

        void setGeofenceState(int transitionType);

        void setGeofencingStarted(boolean started);

    }

    interface DialogsView {

        void requestLocationPermission(int requestId);

        void reportNotReadyError();

        void reportErrorMessage(String errorMessage);
    }

    interface Presenter {

        void start(Context context);

        void stop(Context context);

        void updateGeofenceFromMap(GeofenceData geofenceData);

        void startGeofencing();

        void stopGeofencing();

        void setRandomMockLocation();

        void setCurrentWiFi(Context context);

        void updateGeofenceFromControls(GeofenceData geofenceData);

        GeofenceData getGeofenceData();

        int getCurrentGeofenceState();

        void updateGeofenceState(int newGeofenceState);

        void saveInstanceState(Bundle outState);

        void updateGeofenceAddedState(boolean geofencesAdded);

        boolean geofenceAdded();

        void reportPermissionError(int requestId);

        void reportNotReadyError();

        void reportErrorMessage(String errorMessage);
    }

}
