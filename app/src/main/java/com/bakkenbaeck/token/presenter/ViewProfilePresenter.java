package com.bakkenbaeck.token.presenter;

import android.graphics.Bitmap;
import android.view.View;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.util.ImageUtil;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.util.SharedPrefsUtil;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.ProfileActivity;
import com.bakkenbaeck.token.view.fragment.children.ViewProfileFragment;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class ViewProfilePresenter implements Presenter<ViewProfileFragment> {

    private ViewProfileFragment fragment;
    private User localUser;
    private ProfilePresenter.OnEditButtonListener onEditButtonListener;
    private  OnNextSubscriber<User> handleUserLoaded;

    @Override
    public void onViewAttached(final ViewProfileFragment fragment) {
        this.fragment = fragment;
        initShortLivingObjects();
    }

    private void initShortLivingObjects() {
        attachButtonListeners();
        initToolbar();
        updateView();
        attachListeners();
    }

    private void initToolbar() {
        final ProfileActivity parentActivity = (ProfileActivity) this.fragment.getActivity();
        parentActivity.getBinding().title.setText(R.string.profile);
        parentActivity.getBinding().closeButton.setImageDrawable(parentActivity.getResources().getDrawable(R.drawable.ic_close));
        parentActivity.getBinding().saveButton.setVisibility(View.INVISIBLE);
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
        this.fragment.getBinding().about.setText(this.localUser.getAbout());
        this.fragment.getBinding().location.setText(this.localUser.getLocation());

        final byte[] decodedBitmap = SharedPrefsUtil.getQrCode();
        if (decodedBitmap != null) {
            renderQrCode(decodedBitmap);
        } else {
            generateQrCode();
        }
    }

    private void attachListeners() {
        generateUserLoadedHandler();
        BaseApplication.get()
                .getTokenManager()
                .getUserManager()
                .getUserObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleUserLoaded);
    }

    private void generateUserLoadedHandler() {
        this.handleUserLoaded = new OnNextSubscriber<User>() {
            @Override
            public void onNext(final User user) {
                ViewProfilePresenter.this.localUser = user;
                updateView();
            }
        };
    }

    private void renderQrCode(final byte[] qrCodeImageBytes) {
        final Bitmap qrCodeBitmap = ImageUtil.decodeByteArray(qrCodeImageBytes);
        renderQrCode(qrCodeBitmap);
    }

    private void renderQrCode(final Bitmap qrCodeBitmap) {
        if (this.fragment == null) {
            return;
        }
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
        this.handleUserLoaded.unsubscribe();
        this.handleUserLoaded = null;
    }

    @Override
    public void onViewDestroyed() {
        this.fragment = null;
        this.handleQrCodeGenerated.unsubscribe();
    }

    public void setOnEditButtonListener(final ProfilePresenter.OnEditButtonListener onEditButtonListener) {
        this.onEditButtonListener = onEditButtonListener;
    }
}
