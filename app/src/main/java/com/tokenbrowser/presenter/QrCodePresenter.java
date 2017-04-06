package com.tokenbrowser.presenter;

import android.graphics.Bitmap;

import com.crashlytics.android.Crashlytics;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.QrCodeUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.QrCodeActivity;

import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class QrCodePresenter implements Presenter<QrCodeActivity> {

    private QrCodeActivity activity;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(QrCodeActivity view) {
        this.activity = view;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }
        initShortLivingObjects();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
    }

    private void initShortLivingObjects() {
        fetchUser();
        initClickListeners();
    }

    private void fetchUser() {
        final Subscription sub =
                getCurrentUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::showQrCode,
                        this::handleUserError
                );

        this.subscriptions.add(sub);
    }

    private Single<User> getCurrentUser() {
        return BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .getCurrentUser();
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(v -> this.activity.finish());
    }

    private void handleUserError(final Throwable throwable) {
        LogUtil.e(getClass(), throwable.toString());
    }

    private void showQrCode(final User user) {
        final Subscription sub =
                new QrCodeUtil()
                .generateAddQrCode(user.getUsername())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::renderQrCode,
                        this::handleQrCodeError
                );

        this.subscriptions.add(sub);
    }

    private void renderQrCode(final Bitmap bitmap) {
        if (this.activity == null) return;
        this.activity.getBinding().qrCode.setAlpha(0.0f);
        this.activity.getBinding().qrCode.setImageBitmap(bitmap);
        this.activity.getBinding().qrCode.animate().alpha(1f).setDuration(200).start();
    }

    private void handleQrCodeError(final Throwable throwable) {
        LogUtil.e(getClass(), throwable.toString());
        Crashlytics.logException(throwable);
    }

    @Override
    public void onViewDetached() {
        this.subscriptions.clear();
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.subscriptions = null;
        this.activity = null;
    }
}
