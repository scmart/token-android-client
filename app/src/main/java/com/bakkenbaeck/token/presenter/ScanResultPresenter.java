package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.ScanResult;
import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.presenter.store.ContactStore;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.activity.ScanResultActivity;

import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class ScanResultPresenter implements Presenter<ScanResultActivity> {

    private boolean firstTimeAttached = true;
    private ContactStore contactStore;
    private ScanResultActivity activity;
    private User scannedUser;

    @Override
    public void onViewAttached(final ScanResultActivity activity) {
        this.activity = activity;
        if (this.firstTimeAttached) {
            this.firstTimeAttached = false;
            init();
        }
    }

    private void init() {
        this.contactStore = new ContactStore();
        final Intent intent = activity.getIntent();
        final ScanResult scanResult = intent.getParcelableExtra(ScanResultActivity.EXTRA__RESULT);
        scanResult.getQrCode()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleQrCodeLoaded);
        loadOrFetchContact(scanResult);
    }

    private void loadOrFetchContact(final ScanResult scanResult) {
        this.contactStore
                .load(scanResult.getText())
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
                        scanResult
                                .getContact()
                                .subscribe(new SingleSuccessSubscriber<User>() {
                                    @Override
                                    public void onSuccess(final User user) {
                                        handleUserLoaded(user);
                                    }
                                });
                    }
                });
    }

    private void disableAddContactButton() {
        this.activity.getBinding().addContactButton.setEnabled(false);
        this.activity.getBinding().addContactButton.setText(this.activity.getResources().getString(R.string.added_contact));
    }

    private void handleUserLoaded(final User scannedUser) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                ScanResultPresenter.this.scannedUser = scannedUser;
                ScanResultPresenter.this.activity.getBinding().contactName.setText(scannedUser.getUsername());
                ScanResultPresenter.this.activity.getBinding().addContactButton.setOnClickListener(handleOnAddContact);
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
    }
}
