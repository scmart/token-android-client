package com.bakkenbaeck.token.view;


import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public class RequestPermissionActivity extends AppCompatActivity {

    public interface PermissionsListener {
        void onPermissionGranted(final String permission);
        void onPermissionDenied(final String permission);
    }

    private static final int REQUEST_PERMISSION_ID = 1;
    private PermissionsListener listener;
    private boolean isPermissionGranted = false;

    public void requestPermission(final String permission, final PermissionsListener listener) {
        this.listener = listener;
        if (ContextCompat.checkSelfPermission(this, permission) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{permission},
                    REQUEST_PERMISSION_ID
            );
        } else {
            isPermissionGranted = true;
            listener.onPermissionGranted(permission);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String permissions[],
                                           @NonNull final int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_ID: {
                if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                    isPermissionGranted = true;
                    listener.onPermissionGranted(permissions[0]);
                } else {
                    isPermissionGranted = false;
                    listener.onPermissionDenied(permissions[0]);
                }
            }
        }
    }

    public boolean isPermissionGranted() {
        return isPermissionGranted;
    }
}
