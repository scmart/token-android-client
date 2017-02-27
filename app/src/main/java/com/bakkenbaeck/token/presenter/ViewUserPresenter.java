package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.ActivityScanResultBinding;
import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.util.ImageUtil;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.util.SoundManager;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.ChatActivity;
import com.bakkenbaeck.token.view.activity.ViewUserActivity;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class ViewUserPresenter implements Presenter<ViewUserActivity> {

    private boolean firstTimeAttached = true;
    private ViewUserActivity activity;
    private User scannedUser;

    @Override
    public void onViewAttached(final ViewUserActivity activity) {
        this.activity = activity;
        if (this.firstTimeAttached) {
            this.firstTimeAttached = false;
            initLongLivingObjects();
        }
        initShortLivingObjects();
    }

    private void initLongLivingObjects() {
        final Intent intent = activity.getIntent();
        final String userAddress = intent.getStringExtra(ViewUserActivity.EXTRA__USER_ADDRESS);
        loadOrFetchUser(userAddress);
        generateQrCode(userAddress);
    }

    private void generateQrCode(final String address) {
        ImageUtil
                .generateQrCodeForWalletAddress(address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleQrCodeLoaded);
    }

    private void initShortLivingObjects() {
        this.activity.getBinding().closeButton.setOnClickListener((View v) -> activity.onBackPressed());
    }

    private void loadOrFetchUser(final String userAddress) {
        BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .getUserFromAddress(userAddress)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleUserLoaded, this::handleUserLoadingFailed);
    }

    private void handleUserLoadingFailed(final Throwable throwable) {
        if (this.activity != null) {
            this.activity.finish();
            Toast.makeText(this.activity, R.string.error_unknown_user, Toast.LENGTH_LONG).show();
        }
    }

    private void handleUserLoaded(final User scannedUser) {
        this.scannedUser = scannedUser;
        final ActivityScanResultBinding binding = this.activity.getBinding();
        binding.title.setText(scannedUser.getDisplayName());
        binding.name.setText(scannedUser.getDisplayName());
        binding.username.setText(scannedUser.getUsername());
        binding.about.setText(scannedUser.getAbout());
        binding.location.setText(scannedUser.getLocation());
        binding.addContactButton.setOnClickListener(handleOnAddContact);
        binding.addContactButton.setEnabled(true);
        binding.messageContactButton.setOnClickListener(this::handleMessageContactButton);
        binding.ratingView.setStars(3.6);
        updateAddContactState();
    }

    private void updateAddContactState() {
        final boolean isAContact = isAContact(scannedUser);
        final ToggleButton addContactButton = this.activity.getBinding().addContactButton;
        addContactButton.setChecked(isAContact);
        addContactButton.setSoundEffectsEnabled(isAContact);

        final Drawable checkMark = AppCompatResources.getDrawable(this.activity, R.drawable.ic_done);
        DrawableCompat.setTint(checkMark, this.activity.getResources().getColor(R.color.colorPrimary));
        addContactButton.setCompoundDrawablesWithIntrinsicBounds(isAContact ? checkMark : null, null, null, null);
    }

    private final OnSingleClickListener handleOnAddContact = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            final boolean isAContact = isAContact(scannedUser);
            if (isAContact) {
                deleteContact(scannedUser);
            } else {
                saveContact(scannedUser);
                SoundManager.getInstance().playSound(SoundManager.ADD_CONTACT);
            }
            updateAddContactState();
        }
    };

    private boolean isAContact(final User user) {
        return BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .isUserAContact(user);
    }

    private void deleteContact(final User user) {
        BaseApplication
            .get()
            .getTokenManager()
            .getUserManager()
            .deleteContact(user);
    }

    private void saveContact(final User user) {
        BaseApplication
            .get()
            .getTokenManager()
            .getUserManager()
            .saveContact(user);
    }

    private void handleMessageContactButton(final View view) {
        final Intent intent = new Intent(this.activity, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS, this.scannedUser.getOwnerAddress());
        this.activity.startActivity(intent);
        this.activity.finish();
    }

    private final SingleSuccessSubscriber<Bitmap> handleQrCodeLoaded = new SingleSuccessSubscriber<Bitmap>() {
        @Override
        public void onSuccess(final Bitmap qrBitmap) {
            renderQrCode(qrBitmap);
        }
    };

    private void renderQrCode(final Bitmap qrCodeBitmap) {
        if (this.activity == null || this.activity.getBinding() == null) {
            return;
        }
        this.activity.getBinding().qrCodeImage.setAlpha(0.0f);
        this.activity.getBinding().qrCodeImage.setImageBitmap(qrCodeBitmap);
        this.activity.getBinding().qrCodeImage.animate().alpha(1f).setDuration(200).start();
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        this.activity = null;
        this.handleQrCodeLoaded.unsubscribe();
    }
}
