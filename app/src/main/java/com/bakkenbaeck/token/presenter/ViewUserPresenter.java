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
import com.bakkenbaeck.token.util.ImageUtil;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.activity.ViewUserActivity;

import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class ViewUserPresenter implements Presenter<ViewUserActivity> {

    private boolean firstTimeAttached = true;
    private ContactStore contactStore;
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
        this.activity.getBinding().closeButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(final View v) {
                activity.onBackPressed();
            }
        });
    }



    private void disableAddContactButton() {
        this.activity.getBinding().addContactButton.setEnabled(false);
        this.activity.getBinding().addContactButton.setText(this.activity.getResources().getString(R.string.added_contact));
    }

    private void loadOrFetchUser(final String userAddress) {
        this.contactStore
                .load(userAddress)
                .subscribe(new SingleSubscriber<User>() {
                    @Override
                    public void onSuccess(final User user) {
                        if (user == null) {
                            fetchContactFromServer();
                            return;
                        }

                        disableAddContactButton();
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
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                ViewUserPresenter.this.scannedUser = scannedUser;
                ViewUserPresenter.this.activity.getBinding().name.setText(scannedUser.getUsername());
                ViewUserPresenter.this.activity.getBinding().title.setText(scannedUser.getUsername());
                ViewUserPresenter.this.activity.getBinding().about.setText(scannedUser.getAbout());
                ViewUserPresenter.this.activity.getBinding().location.setText(scannedUser.getLocation());
                ViewUserPresenter.this.activity.getBinding().addContactButton.setOnClickListener(handleOnAddContact);
            }
        });
    };

    private final OnSingleClickListener handleOnAddContact = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            contactStore.save(scannedUser);
            disableAddContactButton();
        }
    };

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
