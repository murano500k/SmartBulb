package com.ashaevy.geofence;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ashaevy.geofence.data.source.FakeGeofenceDataSource;
import com.stc.smartbulb.geofence.data.source.GeofenceDataSource;

/**
 * Enables injection of production implementations for
 * {@link GeofenceDataSource} at compile time.
 */
public class Injection {

    public static GeofenceDataSource provideGeofenceDataSource(@NonNull Context context) {
        return FakeGeofenceDataSource.getInstance();
    }
}
