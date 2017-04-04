package com.tokenbrowser.presenter;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tokenbrowser.BuildConfig;
import com.tokenbrowser.R;
import com.tokenbrowser.model.local.ActivityResultHolder;
import com.tokenbrowser.model.local.PermissionResultHolder;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.network.UserDetails;
import com.tokenbrowser.util.FileUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.OnSingleClickListener;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.fragment.DialogFragment.ChooserDialog;
import com.tokenbrowser.view.activity.EditProfileActivity;

import java.io.File;
import java.io.IOException;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class EditProfilePresenter implements Presenter<EditProfileActivity> {

    private static final int PICK_IMAGE = 1;
    private static final int CAPTURE_IMAGE = 2;
    private static final int CAMERA_PERMISSION = 5;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION = 6;
    private static final String INTENT_TYPE = "image/*";

    private EditProfileActivity activity;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;
    private String displayNameFieldContents;
    private String userNameFieldContents;
    private String aboutFieldContents;
    private String locationFieldContents;
    private String avatarUrl;
    private String cameraImagePath;

    @Override
    public void onViewAttached(final EditProfileActivity view) {
        this.activity = view;
        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            this.subscriptions = new CompositeSubscription();
        }
        initShortLivingObjects();
    }

    private void initShortLivingObjects() {
        initToolbar();
        updateView();
        initClickListeners();
        attachListeners();
    }

    private void initToolbar() {
        this.activity.getBinding().title.setText(R.string.edit_profile);
        this.activity.getBinding().saveButton.setOnClickListener(this.handleSaveClicked);
        this.activity.getBinding().avatar.setOnClickListener(this::handleAvatarClicked);
        this.activity.getBinding().editProfilePhoto.setOnClickListener(this::handleAvatarClicked);
    }

    private void updateView() {
        this.activity.getBinding().inputName.setText(this.displayNameFieldContents);
        this.activity.getBinding().inputUsername.setText(this.userNameFieldContents);
        this.activity.getBinding().inputAbout.setText(this.aboutFieldContents);
        this.activity.getBinding().inputLocation.setText(this.locationFieldContents);
        Glide.with(this.activity)
                .load(this.avatarUrl)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(this.activity.getBinding().avatar);
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(__ -> this.activity.finish());
    }

    private void attachListeners() {
        this.subscriptions.add(BaseApplication.get()
                .getTokenManager()
                .getUserManager()
                .getUserObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleUserLoaded));
    }

    private final OnSingleClickListener handleSaveClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            if (!validate()) return;

            final UserDetails userDetails =
                    new UserDetails()
                    .setDisplayName(activity.getBinding().inputName.getText().toString().trim())
                    .setUsername(activity.getBinding().inputUsername.getText().toString().trim())
                    .setAbout(activity.getBinding().inputAbout.getText().toString().trim())
                    .setLocation(activity.getBinding().inputLocation.getText().toString().trim());

            subscriptions.add(BaseApplication.get()
                    .getTokenManager()
                    .getUserManager()
                    .updateUser(userDetails)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            __ -> handleUserUpdated(),
                            error -> handleUserUpdateFailed(error)));
        }

        private boolean validate() {
            final String displayName = activity.getBinding().inputName.getText().toString().trim();
            final String username = activity.getBinding().inputUsername.getText().toString().trim();

            if (displayName.trim().length() == 0) {
                activity.getBinding().inputName.setError(activity.getResources().getString(R.string.error__required));
                activity.getBinding().inputName.requestFocus();
                return false;
            }

            if (username.trim().length() == 0) {
                activity.getBinding().inputUsername.setError(activity.getResources().getString(R.string.error__required));
                activity.getBinding().inputUsername.requestFocus();
                return false;
            }

            if (username.contains(" ")) {
                activity.getBinding().inputUsername.setError(activity.getResources().getString(R.string.error__invalid_characters));
                activity.getBinding().inputUsername.requestFocus();
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
                checkExternalStoragePermission();
            }
        });
        dialog.show(this.activity.getSupportFragmentManager(), ChooserDialog.TAG);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this.activity,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this.activity,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION);
        } else {
            startCameraActivity();
        }
    }

    private void startCameraActivity() {
        final Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(this.activity.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = new FileUtil().createImageFileWithRandomName(this.activity);
                this.cameraImagePath = photoFile.getAbsolutePath();
            } catch (IOException e) {
                LogUtil.e(getClass(), "Error during creating image file " + e.getMessage());
            }
            if (photoFile != null) {
                final Uri photoURI = FileProvider.getUriForFile(
                        BaseApplication.get(),
                        BuildConfig.APPLICATION_ID + ".photos",
                        photoFile);
                grantUriPermission(cameraIntent, photoURI);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                this.activity.startActivityForResult(cameraIntent, CAPTURE_IMAGE);
            }
        }
    }

    private void grantUriPermission(final Intent intent, final Uri uri) {
        if (Build.VERSION.SDK_INT >= 21) return;
        final PackageManager pm = this.activity.getPackageManager();
        final String packageName = intent.resolveActivity(pm).getPackageName();
        this.activity.grantUriPermission(
                packageName,
                uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_READ_URI_PERMISSION
        );
    }

    private void checkExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this.activity,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this.activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_PERMISSION);
        } else {
            startGalleryActivity();
        }
    }

    private void startGalleryActivity() {
        final Intent pickPictureIntent = new Intent()
                .setType(INTENT_TYPE)
                .setAction(Intent.ACTION_GET_CONTENT);

        if (pickPictureIntent.resolveActivity(this.activity.getPackageManager()) != null) {
            final Intent chooser = Intent.createChooser(
                    pickPictureIntent,
                    BaseApplication.get().getString(R.string.select_picture));
            this.activity.startActivityForResult(chooser, PICK_IMAGE);
        }
    }

    private void handleUserLoaded(final User user) {
        setFieldsFromUser(user);
        updateView();
    }

    private void setFieldsFromUser(final User user) {
        if (this.displayNameFieldContents == null) this.displayNameFieldContents = user.getDisplayName();
        if (this.userNameFieldContents == null) this.userNameFieldContents = user.getUsernameForEditing();
        if (this.aboutFieldContents == null) this.aboutFieldContents = user.getAbout();
        if (this.locationFieldContents == null) this.locationFieldContents = user.getLocation();
        this.avatarUrl = user.getAvatar();
    }

    private void handleUserUpdated() {
        showToast("Saved successfully!");
        this.activity.onBackPressed();
    }

    private void handleUserUpdateFailed(final Throwable error) {
        LogUtil.e(getClass(), error.toString());
        showToast("Error updating profile. Try a different username");
    }

    private void showToast(final String message) {
        Toast.makeText(this.activity, message, Toast.LENGTH_LONG).show();
    }

    private void saveFields() {
        this.displayNameFieldContents = this.activity.getBinding().inputName.getText().toString();
        this.userNameFieldContents = this.activity.getBinding().inputUsername.getText().toString();
        this.aboutFieldContents = this.activity.getBinding().inputAbout.getText().toString();
        this.locationFieldContents = this.activity.getBinding().inputLocation.getText().toString();
    }

    public boolean handleActivityResult(final ActivityResultHolder resultHolder) {
        if (resultHolder.getResultCode() != Activity.RESULT_OK
                || this.activity == null) {
            return false;
        }

        if (resultHolder.getRequestCode() == PICK_IMAGE) {
            try {
                handleGalleryImage(resultHolder);
            } catch (IOException e) {
                LogUtil.e(getClass(), "Error during uploading gallery image");
                showFailureMessage();
                return false;
            }
        } else if (resultHolder.getRequestCode() == CAPTURE_IMAGE) {
            handleCameraImage();
        }

        return true;
    }

    private void handleGalleryImage(final ActivityResultHolder resultHolder) throws IOException {
        final Uri uri = resultHolder.getIntent().getData();
        final FileUtil fileUtil = new FileUtil();
        final File file = fileUtil.saveFileFromUri(this.activity, uri);
        uploadAvatar(file);
    }

    private void handleCameraImage() {
        final File cameraImage = new File(this.cameraImagePath);
        uploadAvatar(cameraImage);
    }

    private void uploadAvatar(final File file) {
        if (file == null) return;
        if (!file.exists()) return;

        final Subscription sub = BaseApplication.get()
                .getTokenManager()
                .getUserManager()
                .uploadAvatar(file)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(unused -> tryDeleteCachedFile(file))
                .doOnError(unused -> tryDeleteCachedFile(file))
                .subscribe(
                        this::handleUploadSuccess,
                        unused -> showFailureMessage()
                );

        this.subscriptions.add(sub);
    }

    private void handleUploadSuccess(final User user) {
        if (this.activity == null) return;
        handleUserLoaded(user);
        Toast.makeText(this.activity, this.activity.getString(R.string.profile_image_success), Toast.LENGTH_SHORT).show();
    }

    private boolean tryDeleteCachedFile(final File file) {
        if (!file.exists()) return true;
        return file.delete();
    }

    private void showFailureMessage() {
        if (this.activity == null) return;
        Toast.makeText(this.activity, this.activity.getString(R.string.profile_image_error), Toast.LENGTH_SHORT).show();
    }

    public boolean handlePermissionResult(final PermissionResultHolder permissionResultHolder) {
        if (permissionResultHolder == null || this.activity == null) return false;
        final int[] grantResults = permissionResultHolder.getGrantResults();
        if (grantResults.length == 0) return true;

        if (permissionResultHolder.getRequestCode() == CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraActivity();
                return true;
            }
        } else if (permissionResultHolder.getRequestCode() == READ_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startGalleryActivity();
                return true;
            }
        }

        return false;
    }

    @Override
    public void onViewDetached() {
        saveFields();
        this.subscriptions.clear();
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.activity = null;
    }
}
