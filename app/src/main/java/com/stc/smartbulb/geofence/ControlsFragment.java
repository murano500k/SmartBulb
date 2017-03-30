package com.stc.smartbulb.geofence;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.stc.smartbulb.R;
import com.stc.smartbulb.geofence.data.GeofenceData;


public class ControlsFragment extends Fragment implements GeofenceContract.ControlsView {

    public static final int CHECK_LATITUDE = 0;
    public static final int CHECK_LONGITUDE = 1;
    public static final int CHECK_RADIUS = 2;
    private GeofenceContract.Presenter mPresenter;

    private TextInputEditText mPointLatInput;
    private TextInputEditText mPointLonInput;
    private TextInputEditText mRadiusInput;
    private TextInputEditText mWiFiNameInput;

    private TextInputLayout mPointLatInputLayout;
    private TextInputLayout mPointLonInputLayout;
    private TextInputLayout mRadiusInputLayout;
    private TextInputLayout mWiFiNameInputLayout;

    private View mStartGeofencingButton;
    private View mStopGeofencingButton;
    private View mSetCurrentWiFiButton;
    private InputWatcher mTextWatcher = new InputWatcher();

    private boolean isResumed = false;

    public ControlsFragment() {
        // Required empty public constructor
    }

    @Override
    public void setPresenter(GeofenceContract.Presenter presenter) {
        mPresenter = presenter;
    }

    private class InputWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            // to avoid updating data when text field is restored from saved state
            if (isResumed) {
                tryUpdatePresenterData(false);
            }
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_controls, container, false);

        mStartGeofencingButton = view.findViewById(R.id.start_geofencing);
        mStopGeofencingButton = view.findViewById(R.id.stop_geofencing);
        mSetCurrentWiFiButton = view.findViewById(R.id.button_set_current_wifi);

        mPointLatInput = (TextInputEditText) view.findViewById(R.id.input_point_lat);
        mPointLonInput = ((TextInputEditText) view.findViewById(R.id.input_point_lon));
        mRadiusInput = ((TextInputEditText) view.findViewById(R.id.input_radius));
        mWiFiNameInput = ((TextInputEditText) view.findViewById(R.id.input_wifi_name));

        mPointLatInputLayout = (TextInputLayout) view.findViewById(R.id.input_layout_point_lat);
        mPointLonInputLayout = ((TextInputLayout) view.findViewById(R.id.input_layout_point_lon));
        mRadiusInputLayout = ((TextInputLayout) view.findViewById(R.id.input_layout_radius));
        mWiFiNameInputLayout = ((TextInputLayout) view.findViewById(R.id.input_layout_wifi_name));

        mSetCurrentWiFiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.setCurrentWiFi(getActivity());
            }
        });

        mStartGeofencingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tryUpdatePresenterData(true)) {
                    mPresenter.startGeofencing();
                }
            }
        });

        mStopGeofencingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.stopGeofencing();
            }
        });

        View randomLocationButton = view.findViewById(R.id.button_random_location);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean useMockLocation = sharedPref.getBoolean(SettingsFragment.
                KEY_GEOFENCE_USE_MOCK_LOCATION, getResources().getBoolean(R.bool.
                pref_geofenceUseMockLocationDefault));
        if (useMockLocation) {
            randomLocationButton.setVisibility(View.VISIBLE);
        } else {
            randomLocationButton.setVisibility(View.GONE);
        }
        randomLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.setRandomMockLocation();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isResumed = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isResumed = false;
    }

    private void registerTextWatcher() {
        mPointLatInput.addTextChangedListener(mTextWatcher);
        mPointLonInput.addTextChangedListener(mTextWatcher);
        mRadiusInput.addTextChangedListener(mTextWatcher);
        mWiFiNameInput.addTextChangedListener(mTextWatcher);
    }

    private void unregisterTextWatcher() {
        mPointLatInput.removeTextChangedListener(mTextWatcher);
        mPointLonInput.removeTextChangedListener(mTextWatcher);
        mRadiusInput.removeTextChangedListener(mTextWatcher);
        mWiFiNameInput.removeTextChangedListener(mTextWatcher);
    }

    @Override
    public void updateGeofence(GeofenceData geofenceData) {
        unregisterTextWatcher();

        mPointLatInput.setText(String.valueOf(geofenceData.getLatitude()));
        mPointLonInput.setText(String.valueOf(geofenceData.getLongitude()));
        mRadiusInput.setText(String.valueOf(geofenceData.getRadius()));
        mWiFiNameInput.setText(geofenceData.getWifiName());

        registerTextWatcher();
    }

    @Override
    public void setGeofenceState(int geofenceState) {
        View view = getView();
        if (view != null) {
            switch (geofenceState) {
                case Constants.GEOFENCE_STATE_INSIDE:
                    ((TextView) view.findViewById(R.id.geofence_state)).setText(R.string.geofence_state_inside);
                    return;
                case Constants.GEOFENCE_STATE_OUTSIDE:
                    ((TextView) view.findViewById(R.id.geofence_state)).setText(R.string.geofence_state_outsize);
                    return;
                default:
                    ((TextView) view.findViewById(R.id.geofence_state)).setText(R.string.geofence_state_unknown);
            }
        }
    }

    @Override
    public void setGeofencingStarted(boolean started) {
        mPointLatInput.setEnabled(!started);
        mPointLonInput.setEnabled(!started);
        mRadiusInput.setEnabled(!started);
        mWiFiNameInput.setEnabled(!started);

        mStartGeofencingButton.setEnabled(!started);
        mSetCurrentWiFiButton.setEnabled(!started);
        mStopGeofencingButton.setEnabled(started);
    }

    public boolean tryUpdatePresenterData(boolean reportError) {
        try {
            GeofenceData geofenceData = new GeofenceData();
            geofenceData.setLatitude(doubleInputValidation(mPointLatInput, mPointLatInputLayout, CHECK_LATITUDE));
            geofenceData.setLongitude(doubleInputValidation(mPointLonInput, mPointLonInputLayout, CHECK_LONGITUDE));
            geofenceData.setRadius(doubleInputValidation(mRadiusInput, mRadiusInputLayout, CHECK_RADIUS));

            Editable text = mWiFiNameInput.getText();
            if (!TextUtils.isEmpty(text)) {
                geofenceData.setWifiName(text.toString());
                mWiFiNameInputLayout.setErrorEnabled(false);
            } else {
                mWiFiNameInputLayout.setError(getString(R.string.empty_value_validation_error));
                throw new ValidationException();
            }

            mPresenter.updateGeofenceFromControls(geofenceData);

            return true;
        } catch (ValidationException e) {

            if (reportError) {
                showErrorDialog();
            }

            return false;
        }
    }

    private void showErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(R.string.validation_error)
                .setTitle(R.string.validation_error_dialog_title);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private double doubleInputValidation(TextInputEditText doubleTextInputEditText,
                                         TextInputLayout doubleTextInputLayout, int check) {
        Editable editable = doubleTextInputEditText.getText();
        if (TextUtils.isEmpty(editable)) {
            doubleTextInputLayout.setError(getString(R.string.empty_value_validation_error));
            throw new ValidationException();
        }
        try {
            double result = Double.parseDouble(editable.toString());
            switch (check) {
                case  CHECK_LATITUDE: if (result < -90 || result > 90) {
                    doubleTextInputLayout.setError(getString(R.string.latitude_validation_error));
                    throw new ValidationException();
                }
                    break;
                case  CHECK_LONGITUDE: if (result < -180 || result > 180) {
                    doubleTextInputLayout.setError(getString(R.string.longitude_validation_error));
                    throw new ValidationException();
                }
                    break;
                case CHECK_RADIUS: if (result <= 0) {
                    doubleTextInputLayout.setError(getString(R.string.radius_validation_error));
                    throw new ValidationException();
                }
            }
            doubleTextInputLayout.setErrorEnabled(false);
            return result;
        } catch (NumberFormatException e) {
            doubleTextInputLayout.setError(getString(R.string.double_validation_error));
            throw new ValidationException();
        }
    }

    static class ValidationException extends RuntimeException {
        ValidationException() {}
    }

}
