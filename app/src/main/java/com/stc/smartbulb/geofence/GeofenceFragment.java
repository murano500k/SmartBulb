package com.stc.smartbulb.geofence;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ashaevy.geofence.Injection;
import com.stc.smartbulb.R;
import com.stc.smartbulb.geofence.data.source.GeofenceDataSource;
import com.stc.smartbulb.geofence.transition.GeofenceHelper;
import com.stc.smartbulb.geofence.transition.GooglePlayGeofenceHelper;
import com.stc.smartbulb.geofence.transition.LocationBasedGeofenceHelper;

/**
 * Fragment that contains main application UI.
 */
public class GeofenceFragment extends Fragment {

    private GeofencePresenter mGeofencePresenter;

    public GeofenceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_geofence, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GeofenceContract.MapView mapView = ((GeofenceContract.MapView) getChildFragmentManager().
                findFragmentById(R.id.map));
        GeofenceContract.ControlsView controlsView = ((GeofenceContract.ControlsView)
                getChildFragmentManager().findFragmentById(R.id.controls));

        DialogsFragment dialogsFragment = ((DialogsFragment) getFragmentManager().
                findFragmentByTag(Constants.DIALOGS_FRAGMENT_TAG));
        if (dialogsFragment == null) {
            dialogsFragment = new DialogsFragment();
            getChildFragmentManager().beginTransaction().add(dialogsFragment,
                    Constants.DIALOGS_FRAGMENT_TAG).commit();
        }

        GeofenceDataSource geofenceDataSource = Injection.provideGeofenceDataSource(getActivity());

        GeofenceHelper geofenceHelper;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String geofenceDetectionProvider = sharedPref.getString(SettingsFragment.
                KEY_GEOFENCE_DETECTION_PROVIDER, getString(R.string.
                pref_geofenceDetectionProviders_default));
        if (geofenceDetectionProvider.equals(getString(R.string.
                pref_geofenceDetectionProviders_gp_location))) {
            geofenceHelper = new LocationBasedGeofenceHelper(getActivity(), geofenceDataSource);
        } else if (geofenceDetectionProvider.equals(getString(R.string.
                pref_geofenceDetectionProviders_gp_geofence))) {
            geofenceHelper = new GooglePlayGeofenceHelper(getActivity());
        } else {
            throw new IllegalArgumentException("Invalid Geofence Detection Provider.");
        }

        GeofencePresenter.Views views = new GeofencePresenter.Views(mapView,
                controlsView, dialogsFragment);

        boolean useMockLocation = sharedPref.getBoolean(SettingsFragment.
                KEY_GEOFENCE_USE_MOCK_LOCATION, getResources().getBoolean(R.bool.
                pref_geofenceUseMockLocationDefault));

        mGeofencePresenter = new GeofencePresenter(geofenceDataSource, views,
                geofenceHelper);
        mGeofencePresenter.setUseMockLocation(useMockLocation);
        mGeofencePresenter.create(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGeofencePresenter.start(getActivity());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mGeofencePresenter.saveInstanceState(outState);
    }

    @Override
    public void onStop() {
        mGeofencePresenter.stop(getActivity());
        super.onStop();
    }
}
