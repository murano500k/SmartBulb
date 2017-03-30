package com.stc.smartbulb.geofence.data.source;


import com.stc.smartbulb.geofence.data.GeofenceData;

/**
 * Interface for Data Source that stores all geofence data.
 */
public interface GeofenceDataSource {
    void saveGeofenceData(GeofenceData geofenceData);
    GeofenceData readGeofenceData();

    boolean geofenceAdded();
    void saveGeofenceAdded(boolean geofenceAdded);

    void saveGeofenceTransition(int transition);
    int readGeofenceTransition();
}
