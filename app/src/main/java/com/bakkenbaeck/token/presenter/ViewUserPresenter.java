package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.network.IdService;
import com.bakkenbaeck.token.presenter.store.ContactStore;
import com.bakkenbaeck.token.presenter.store.UserStore;
import com.bakkenbaeck.token.util.ImageUtil;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.util.SoundManager;
import com.bakkenbaeck.token.view.activity.ChatActivity;
import com.bakkenbaeck.token.view.activity.ViewUserActivity;

import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class ViewUserPresenter implements Presenter<ViewUserActivity> {

    private boolean firstTimeAttached = true;
    private ContactStore contactStore;
    private UserStore userStore;
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
        this.contactStore = new ContactStore();
        this.userStore = new UserStore();
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
        this.userStore
                .loadForAddress(userAddress)
                .subscribe(new SingleSubscriber<User>() {
                    @Override
                    public void onSuccess(final User user) {
                        if (user == null) {
                            fetchContactFromServer();
                            return;
                        }

                        handleUserLoaded(user);
                    }

                    @Override
                    public void onError(final Throwable error) {
                        fetchContactFromServer();
                    }

                    private void fetchContactFromServer() {
                        IdService
                                .getApi()
                                .getUser(userAddress)
                                .subscribe(new SingleSuccessSubscriber<User>() {
                                    @Override
                                    public void onSuccess(final User user) {
                                        handleUserLoaded(user);
                                    }
                                });
                    }
                });
    }

    private void handleUserLoaded(final User scannedUser) {
        new Handler(Looper.getMainLooper()).post(() -> {
            ViewUserPresenter.this.scannedUser = scannedUser;
            ViewUserPresenter.this.activity.getBinding().title.setText(scannedUser.getDisplayName());
            ViewUserPresenter.this.activity.getBinding().name.setText(scannedUser.getDisplayName());
            ViewUserPresenter.this.activity.getBinding().username.setText(scannedUser.getUsername());
            ViewUserPresenter.this.activity.getBinding().about.setText(scannedUser.getAbout());
            ViewUserPresenter.this.activity.getBinding().location.setText(scannedUser.getLocation());
            ViewUserPresenter.this.activity.getBinding().addContactButton.setOnClickListener(handleOnAddContact);
            ViewUserPresenter.this.activity.getBinding().addContactButton.setEnabled(true);
            ViewUserPresenter.this.activity.getBinding().messageContactButton.setOnClickListener(ViewUserPresenter.this::handleMessageContactButton);
            ViewUserPresenter.this.activity.getBinding().ratingView.setStars(3.6);
            updateAddContactState();
        });
    }

    private void updateAddContactState() {
        final boolean isAContact = contactStore.userIsAContact(scannedUser);
        if (isAContact) {
            this.activity.getBinding().addContactButton.setText(this.activity.getResources().getString(R.string.remove_contact));
            this.activity.getBinding().addContactButton.setSoundEffectsEnabled(true);
        } else {
            this.activity.getBinding().addContactButton.setText(this.activity.getResources().getString(R.string.add_contact));
            this.activity.getBinding().addContactButton.setSoundEffectsEnabled(false);
        }
    }

    private final OnSingleClickListener handleOnAddContact = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            final boolean isAContact = contactStore.userIsAContact(scannedUser);
            if (isAContact) {
                contactStore.delete(scannedUser);
            } else {
                contactStore.save(scannedUser);
                SoundManager.getInstance().playSound(SoundManager.ADD_CONTACT);
            }
            updateAddContactState();
        }
    };

    private void handleMessageContactButton(final View view) {
        final Intent intent = new Intent(this.activity, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA__REMOTE_USER, this.scannedUser);
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
