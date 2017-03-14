package com.tokenbrowser.presenter;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Toast;

import com.tokenbrowser.model.local.ActivityResultHolder;
import com.tokenbrowser.model.local.PermissionResultHolder;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.network.UserDetails;
import com.tokenbrowser.token.BuildConfig;
import com.tokenbrowser.token.R;
import com.tokenbrowser.util.FileUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.OnNextSubscriber;
import com.tokenbrowser.util.OnSingleClickListener;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.ProfileActivity;
import com.tokenbrowser.view.fragment.DialogFragment.ChooserDialog;
import com.tokenbrowser.view.fragment.children.EditProfileFragment;

import java.io.File;
import java.io.IOException;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class EditProfilePresenter implements Presenter<EditProfileFragment> {

    private static final int PICK_IMAGE = 1;
    private static final int CAPTURE_IMAGE = 2;
    private static final int CAMERA_PERMISSION = 5;
    private static final String INTENT_TYPE = "image/*";

    private OnNextSubscriber<User> handleUserLoaded;
    private EditProfileFragment fragment;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;
    private String displayNameFieldContents;
    private String userNameFieldContents;
    private String aboutFieldContents;
    private String locationFieldContents;
    private String captureImageFilename;

    @Override
    public void onViewAttached(final EditProfileFragment fragment) {
        this.fragment = fragment;
        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            this.subscriptions = new CompositeSubscription();
        }
        initShortLivingObjects();
    }

    private void initShortLivingObjects() {
        initToolbar();
        updateView();
        attachListeners();
    }

    private void initToolbar() {
        final ProfileActivity parentActivity = (ProfileActivity) this.fragment.getActivity();
        parentActivity.getBinding().title.setText(R.string.edit_profile);
        parentActivity.getBinding().closeButton.setImageResource(R.drawable.ic_arrow_back);
        parentActivity.getBinding().saveButton.setVisibility(View.VISIBLE);
        parentActivity.getBinding().saveButton.setOnClickListener(this.handleSaveClicked);
        this.fragment.getBinding().avatar.setOnClickListener(this::handleAvatarClicked);
    }

    private void updateView() {
        this.fragment.getBinding().inputName.setText(this.displayNameFieldContents);
        this.fragment.getBinding().inputUsername.setText(this.userNameFieldContents);
        this.fragment.getBinding().inputAbout.setText(this.aboutFieldContents);
        this.fragment.getBinding().inputLocation.setText(this.locationFieldContents);
    }

    private final OnSingleClickListener handleSaveClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            if (!validate()) {
                return;
            }
            final UserDetails userDetails =
                    new UserDetails()
                    .setDisplayName(fragment.getBinding().inputName.getText().toString().trim())
                    .setUsername(fragment.getBinding().inputUsername.getText().toString().trim())
                    .setAbout(fragment.getBinding().inputAbout.getText().toString().trim())
                    .setLocation(fragment.getBinding().inputLocation.getText().toString().trim());

            subscriptions.add(BaseApplication.get()
                    .getTokenManager()
                    .getUserManager()
                    .updateUser(userDetails)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            user -> handleUserUpdated(user),
                            error -> handleUserUpdateFailed(error)));
        }

        private boolean validate() {
            final String displayName = fragment.getBinding().inputName.getText().toString().trim();
            final String username = fragment.getBinding().inputUsername.getText().toString().trim();

            if (displayName.trim().length() == 0) {
                fragment.getBinding().inputName.setError(fragment.getResources().getString(R.string.error__required));
                fragment.getBinding().inputName.requestFocus();
                return false;
            }

            if (username.trim().length() == 0) {
                fragment.getBinding().inputUsername.setError(fragment.getResources().getString(R.string.error__required));
                fragment.getBinding().inputUsername.requestFocus();
                return false;
            }

            if (username.contains(" ")) {
                fragment.getBinding().inputUsername.setError(fragment.getResources().getString(R.string.error__invalid_characters));
                fragment.getBinding().inputUsername.requestFocus();
                return false;
            }
            return true;
        }
    };

    private void handleAvatarClicked(final View v) {
        final ChooserDialog dialog = ChooserDialog.newInstance();
        dialog.setOnChooserClickListener(new ChooserDialog.OnChooserClickListener() {
            @Override
            public void captureImageClicked() {
                checkCameraPermission();
            }

            @Override
            public void importImageFromGalleryClicked() {
                startGalleryActivity();
            }
        });
        dialog.show(this.fragment.getChildFragmentManager(), ChooserDialog.TAG);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this.fragment.getContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            this.fragment.requestPermissions(
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION);
        } else {
            startCameraActivity();
        }
    }

    private void startCameraActivity() {
        final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(this.fragment.getContext().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = new FileUtil().createImageFileWithRandomName(this.fragment.getContext());
                this.captureImageFilename = photoFile.getName();
            } catch (IOException e) {
                LogUtil.e(getClass(), "Error during creating image file " + e.getMessage());
            }
            if (photoFile != null) {
                final Uri photoURI = FileProvider.getUriForFile(
                        BaseApplication.get(),
                        BuildConfig.APPLICATION_ID + ".photos",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                this.fragment.startActivityForResult(takePictureIntent, CAPTURE_IMAGE);
            }
        }
    }

    private void startGalleryActivity() {
        final Intent pickPictureIntent = new Intent()
                .setType(INTENT_TYPE)
                .setAction(Intent.ACTION_GET_CONTENT);

        if (pickPictureIntent.resolveActivity(this.fragment.getContext().getPackageManager()) != null) {
            final Intent chooser = Intent.createChooser(
                    pickPictureIntent,
                    BaseApplication.get().getString(R.string.select_picture));
            this.fragment.startActivityForResult(chooser, PICK_IMAGE);
        }
    }

    private void attachListeners() {
        generateUserLoadedHandler();
        this.subscriptions.add(BaseApplication.get()
                .getTokenManager()
                .getUserManager()
                .getUserObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleUserLoaded));
    }

    private void generateUserLoadedHandler() {
        this.handleUserLoaded = new OnNextSubscriber<User>() {
            @Override
            public void onNext(final User user) {
                setFieldsFromUser(user);
                updateView();
            }

            private void setFieldsFromUser(final User user) {
                if (displayNameFieldContents == null) displayNameFieldContents = user.getDisplayName();
                if (userNameFieldContents == null) userNameFieldContents = user.getUsernameForEditing();
                if (aboutFieldContents == null) aboutFieldContents = user.getAbout();
                if (locationFieldContents == null) locationFieldContents = user.getLocation();
            }
        };
    }

    private void handleUserUpdated(final User updatedUser) {
        showToast("Saved successfully!");
        fragment.getActivity().onBackPressed();
    }

    private void handleUserUpdateFailed(final Throwable error) {
        LogUtil.e(getClass(), error.toString());
        showToast("Error updating profile. Try a different username");
    }

    private void showToast(final String message) {
        Toast.makeText(fragment.getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onViewDetached() {
        saveFields();
        this.subscriptions.clear();
        this.fragment = null;
        this.handleUserLoaded.unsubscribe();
    }

    private void saveFields() {
        this.displayNameFieldContents = this.fragment.getBinding().inputName.getText().toString();
        this.userNameFieldContents = this.fragment.getBinding().inputUsername.getText().toString();
        this.aboutFieldContents = this.fragment.getBinding().inputAbout.getText().toString();
        this.locationFieldContents = this.fragment.getBinding().inputLocation.getText().toString();
    }

    public boolean handleActivityResult(final ActivityResultHolder resultHolder) {
        if (resultHolder.getResultCode() != Activity.RESULT_OK
                || this.fragment == null) {
            return false;
        }

        if (resultHolder.getRequestCode() == PICK_IMAGE) {
            //Upload image
        } else if (resultHolder.getRequestCode() == CAPTURE_IMAGE) {
            //Upload image
        }

        return true;
    }

    public boolean handlePermissionResult(final PermissionResultHolder permissionResultHolder) {
        if (permissionResultHolder == null || this.fragment == null) return false;

        if (permissionResultHolder.getRequestCode() == CAMERA_PERMISSION) {
            final int[] grantResults = permissionResultHolder.getGrantResults();
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraActivity();
                return true;
            }
        }

        return false;
    }

    @Override
    public void onDestroyed() {
        this.fragment = null;
    }
}
