package com.bakkenbaeck.token.presenter;

import android.graphics.Bitmap;
import android.view.View;

import com.bakkenbaeck.token.model.network.App;
import com.bakkenbaeck.token.util.ImageUtil;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.activity.ViewAppActivity;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ViewAppPresenter implements Presenter<ViewAppActivity> {

    private ViewAppActivity activity;
    private App app;

    @Override
    public void onViewAttached(ViewAppActivity view) {
        this.activity = view;
        getIntentData();
        initView();
        initClickListeners();
    }

    private void getIntentData() {
        this.app = this.activity.getIntent().getParcelableExtra(ViewAppActivity.APP);
    }

    private void initView() {
        this.activity.getBinding().title.setText(app.getDisplayName());
        activity.getBinding().name.setText(app.getDisplayName());
        activity.getBinding().username.setText(app.getDisplayName());
        activity.getBinding().about.setText(app.getLanguages().toString());
        activity.getBinding().location.setText(app.getDisplayName());
        activity.getBinding().ratingView.setStars(3.6);
        generateQrCode(this.app.getOwnerAddress());
    }

    private void generateQrCode(final String address) {
        ImageUtil
                .generateQrCodeForWalletAddress(address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleQrCodeLoaded);
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

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(this::handleClosedClicked);
    }

    private void handleClosedClicked(final View view) {
        this.activity.finish();
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
