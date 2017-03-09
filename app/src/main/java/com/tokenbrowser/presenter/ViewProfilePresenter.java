package com.tokenbrowser.presenter;

import android.graphics.Bitmap;
import android.view.View;

import com.tokenbrowser.model.network.ReputationScore;
import com.tokenbrowser.token.R;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.util.ImageUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.OnSingleClickListener;
import com.tokenbrowser.util.SharedPrefsUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.ProfileActivity;
import com.tokenbrowser.view.fragment.children.ViewProfileFragment;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public final class ViewProfilePresenter implements Presenter<ViewProfileFragment> {

    private ViewProfileFragment fragment;
    private User localUser;
    private ProfilePresenter.OnEditButtonListener onEditButtonListener;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;

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
        this.subscriptions = new CompositeSubscription();
    }

    private void initShortLivingObjects() {
        attachButtonListeners();
        initToolbar();
        updateView();
        fetchUser();
    }

    private void initToolbar() {
        final ProfileActivity parentActivity = (ProfileActivity) this.fragment.getActivity();
        parentActivity.getBinding().title.setText(R.string.profile);
        parentActivity.getBinding().closeButton.setImageResource(R.drawable.ic_arrow_back);
        parentActivity.getBinding().saveButton.setVisibility(View.INVISIBLE);
    }

    private void attachButtonListeners() {
        this.fragment.getBinding().editProfileButton.setOnClickListener(this.editProfileClicked);
    }

    private void updateView() {
        if (this.localUser == null || this.fragment == null) {
            return;
        }

        this.fragment.getBinding().name.setText(this.localUser.getDisplayName());
        this.fragment.getBinding().username.setText(this.localUser.getUsername());
        this.fragment.getBinding().about.setText(this.localUser.getAbout());
        this.fragment.getBinding().location.setText(this.localUser.getLocation());

        final byte[] decodedBitmap = SharedPrefsUtil.getQrCode();
        if (decodedBitmap != null) {
            renderQrCode(decodedBitmap);
        } else {
            generateQrCode();
        }
    }

    private void fetchUser() {
        final Subscription sub = BaseApplication.get()
                .getTokenManager()
                .getUserManager()
                .getUserObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleUserLoaded);

        this.subscriptions.add(sub);
    }

    private void handleUserLoaded(final User user) {
        this.localUser = user;
        updateView();
        fetchUserReputation(user.getTokenId());
    }

    private void fetchUserReputation(final String userAddress) {
        final Subscription reputationSub = BaseApplication
                .get()
                .getTokenManager()
                .getReputationManager()
                .getReputationScore(userAddress)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleReputationResponse, this::handleReputationError);

        this.subscriptions.add(reputationSub);
    }

    private void handleReputationResponse(final ReputationScore reputationScore) {
        if (this.fragment == null) {
            return;
        }
        final String reviewCount = String.valueOf(reputationScore.getCount());
        final double score = reputationScore.getScore() != null ? reputationScore.getScore() : 0;
        final String stringScore = String.valueOf(score);
        this.fragment.getBinding().reviewCount.setText(reviewCount);
        this.fragment.getBinding().ratingView.setStars(score);
        this.fragment.getBinding().reputationScore.setText(stringScore);
        this.fragment.getBinding().ratingInfo.setRatingInfo(reputationScore);
    }

    private void handleReputationError(final Throwable throwable) {
        LogUtil.e(getClass(), "Error during reputation fetching " + throwable.getMessage());
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
        final Subscription sub = ImageUtil.generateQrCodeForWalletAddress(this.localUser.getTokenId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleQrCodeGenerated);

        this.subscriptions.add(sub);
    }

    private void handleQrCodeGenerated(final Bitmap bitmap) {
        SharedPrefsUtil.saveQrCode(ImageUtil.compressBitmap(bitmap));
        renderQrCode(bitmap);
    }

    private final OnSingleClickListener editProfileClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            if (onEditButtonListener == null) {
                return;
            }

            onEditButtonListener.onClick();
        }
    };

    public void setOnEditButtonListener(final ProfilePresenter.OnEditButtonListener onEditButtonListener) {
        this.onEditButtonListener = onEditButtonListener;
    }

    @Override
    public void onViewDetached() {
        this.fragment = null;
    }

    @Override
    public void onViewDestroyed() {
        this.subscriptions.clear();
        this.fragment = null;
    }
}
