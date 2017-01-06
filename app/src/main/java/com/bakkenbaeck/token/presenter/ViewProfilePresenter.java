package com.bakkenbaeck.token.presenter;

import android.graphics.Bitmap;
import android.view.View;

import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.util.ImageUtil;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.util.SharedPrefsUtil;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.fragment.children.ViewProfileFragment;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class ViewProfilePresenter implements Presenter<ViewProfileFragment> {

    private ViewProfileFragment fragment;
    private boolean firstTimeAttaching = true;
    private User localUser;
    private ProfilePresenter.OnEditButtonListener onEditButtonListener;

    @Override
    public void onViewAttached(final ViewProfileFragment fragment) {
        this.fragment = fragment;
        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }

        initShortLivingObjects();
    }

    private void initLongLivingObjects() {
        BaseApplication.get()
                .getTokenManager()
                .getUserManager()
                .getUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleUserLoaded);
    }

    private void initShortLivingObjects() {
        attachButtonListeners();
        updateView();
    }

    private void attachButtonListeners() {
        this.fragment.getBinding().editProfileButton.setOnClickListener(this.editProfileClicked);
    }

    private void updateView() {
        if (this.localUser == null) {
            return;
        }

        this.fragment.getBinding().name.setText(this.localUser.getUsername());
        this.fragment.getBinding().username.setText(this.localUser.getAddress());

        final byte[] decodedBitmap = SharedPrefsUtil.getQrCode();
        if (decodedBitmap != null) {
            renderQrCode(decodedBitmap);
        } else {
            generateQrCode();
        }
    }

    private final SingleSuccessSubscriber<User> handleUserLoaded = new SingleSuccessSubscriber<User>() {
        @Override
        public void onSuccess(final User user) {
            ViewProfilePresenter.this.localUser = user;
            updateView();
            this.unsubscribe();
        }
    };

    private void renderQrCode(final byte[] qrCodeImageBytes) {
        final Bitmap qrCodeBitmap = ImageUtil.decodeByteArray(qrCodeImageBytes);
        renderQrCode(qrCodeBitmap);
    }

    private void renderQrCode(final Bitmap qrCodeBitmap) {
        this.fragment.getBinding().qrCodeImage.setAlpha(0.0f);
        this.fragment.getBinding().qrCodeImage.setImageBitmap(qrCodeBitmap);
        this.fragment.getBinding().qrCodeImage.animate().alpha(1f).setDuration(200).start();
    }

    private void generateQrCode() {
        ImageUtil.generateQrCodeForWalletAddress(this.localUser.getAddress())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleQrCodeGenerated);
    }

    private SingleSuccessSubscriber<Bitmap> handleQrCodeGenerated = new SingleSuccessSubscriber<Bitmap>() {
        @Override
        public void onSuccess(final Bitmap qrBitmap) {
            SharedPrefsUtil.saveQrCode(ImageUtil.compressBitmap(qrBitmap));
            renderQrCode(qrBitmap);
        }
    };

    private final OnSingleClickListener editProfileClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            if (onEditButtonListener == null) {
                return;
            }

            onEditButtonListener.onClick();
        }
    };

    @Override
    public void onViewDetached() {
        this.fragment = null;
    }

    @Override
    public void onViewDestroyed() {
        this.fragment = null;
        this.handleUserLoaded.unsubscribe();
        this.handleQrCodeGenerated.unsubscribe();
    }

    public void setOnEditButtonListener(final ProfilePresenter.OnEditButtonListener onEditButtonListener) {
        this.onEditButtonListener = onEditButtonListener;
    }
}
