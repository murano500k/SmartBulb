package com.stc.smartbulb.geofence;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.stc.smartbulb.R;

/**
 * Fragment that configures usage of Geofence Detection API.
 */

public class SettingsFragment extends PreferenceFragmentCompat {
    public static final String KEY_GEOFENCE_DETECTION_PROVIDER = "pref_geofenceDetectionProvider";
    public static final String KEY_GEOFENCE_USE_MOCK_LOCATION = "pref_geofenceUseMockLocation";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
