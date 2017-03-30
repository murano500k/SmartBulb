package com.stc.smartbulb.geofence;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.stc.smartbulb.R;
import com.stc.smartbulb.utils.PermissionUtils;

/**
 * Fragment without UI to show dialogs.
 */

public class DialogsFragment extends Fragment implements GeofenceContract.DialogsView {

    private boolean mShowPermissionDeniedDialog = false;

    /**
     * Requests the fine location permission. If a rationale with an additional explanation should
     * be shown to the user, displays a dialog that triggers the request.
     * @param requestCode
     */
    @Override
    public void requestLocationPermission(int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Display a dialog with rationale.
            PermissionUtils.RationaleDialog
                    .newInstance(requestCode, false, Constants.DIALOGS_FRAGMENT_TAG).show(
                    getFragmentManager(), Constants.PERMISSION_DIALOG_FRAGMENT_TAG);
        } else {
            // Location permission has not been granted yet, request it.
            PermissionUtils.requestPermissionFromFragment(this, requestCode,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, false, Constants.DIALOGS_FRAGMENT_TAG);
        }
    }

    @Override
    public void reportNotReadyError() {
        Toast.makeText(getActivity(),
                getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void reportErrorMessage(String errorMessage) {
        Toast.makeText(getActivity(),
                errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == Constants.LOCATION_PERMISSION_REQUEST_CODE) {
            // Enable the My Location button if the permission has been granted.
            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                mShowPermissionDeniedDialog = false;
            } else {
                mShowPermissionDeniedDialog = true;
            }

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mShowPermissionDeniedDialog) {
            mShowPermissionDeniedDialog = true;
            PermissionUtils.PermissionDeniedDialog
                    .newInstance(false).show(getFragmentManager(),
                    Constants.PERMISSION_DENIED_DIALOG_FRAGMENT_TAG);
        }
    }

}
