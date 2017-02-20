package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Toast;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.model.network.App;
import com.bakkenbaeck.token.network.DirectoryService;
import com.bakkenbaeck.token.util.ImageUtil;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.ChatActivity;
import com.bakkenbaeck.token.view.activity.ViewAppActivity;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ViewAppPresenter implements Presenter<ViewAppActivity> {

    private ViewAppActivity activity;
    private App app;
    private User user;
    private Subscription userSubscription;

    @Override
    public void onViewAttached(ViewAppActivity view) {
        this.activity = view;
        getIntentData();
        fetchUserFromApp();
        initView();
        initClickListeners();
    }

    private void fetchUserFromApp() {
        if (this.userSubscription != null) {
            this.userSubscription.unsubscribe();
        }

        this.userSubscription = BaseApplication
            .get()
            .getTokenManager()
            .getUserManager()
            .getUserFromAddress(this.app.getOwnerAddress())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleUserLoaded);
    }

    private void getIntentData() {
        final String appOwnerAddress = this.activity.getIntent().getStringExtra(ViewAppActivity.APP_OWNER_ADDRESS);
        this.app = DirectoryService.getCachedApp(appOwnerAddress);
        if (this.app == null) {
            Toast.makeText(this.activity, R.string.error__app_loading, Toast.LENGTH_LONG).show();
            this.activity.finish();
        }
    }

    private void initView() {
        this.activity.getBinding().title.setText(app.getDisplayName());
        activity.getBinding().name.setText(app.getDisplayName());
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
        this.activity.getBinding().messageContactButton.setOnClickListener(this::handleOnMessageClicked);
    }

    private void handleClosedClicked(final View view) {
        this.activity.finish();
    }

    private void handleOnMessageClicked(final View v) {
        final Intent intent = new Intent(this.activity, ChatActivity.class);
        if (this.user != null) {
            intent.putExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS, this.user.getOwnerAddress());
        }
        intent.putExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS, this.user.getOwnerAddress());
        this.activity.startActivity(intent);
    }

    private void handleUserLoaded(final User user) {
        this.user = user;
        if (this.user == null || this.activity == null) {
            return;
        }
        this.activity.getBinding().username.setText(user.getDisplayName());
        this.activity.getBinding().username.setText(user.getUsername());
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        this.activity = null;
        this.userSubscription.unsubscribe();
    }
}
