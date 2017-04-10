package com.tokenbrowser.presenter;

import android.content.Intent;

import com.tokenbrowser.R;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.network.ReputationScore;
import com.tokenbrowser.util.ImageUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.EditProfileActivity;
import com.tokenbrowser.view.activity.ViewProfileActivity;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public final class ViewProfilePresenter implements Presenter<ViewProfileActivity> {

    private ViewProfileActivity activity;
    private User localUser;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(final ViewProfileActivity view) {
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
        initClickListeners();
        attachButtonListeners();
        updateView();
        fetchUser();
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(__ -> this.activity.finish());
        this.activity.getBinding().editProfileButton.setOnClickListener(__ -> goToEditProfileActivity());
    }

    private void goToEditProfileActivity() {
        final Intent intent = new Intent(this.activity, EditProfileActivity.class);
        this.activity.startActivity(intent);
    }

    private void attachButtonListeners() {
        final Subscription sub =
                BaseApplication.get()
                .isConnectedSubject()
                .subscribe(this::handleConnectionChanged);
        this.subscriptions.add(sub);
    }

    private void handleConnectionChanged(final Boolean isConnected) {
        if (this.activity == null) return;
        this.activity.getBinding().editProfileButton.setEnabled(isConnected);
    }

    private void updateView() {
        if (this.localUser == null || this.activity == null) return;
        this.activity.getBinding().name.setText(this.localUser.getDisplayName());
        this.activity.getBinding().username.setText(this.localUser.getUsername());
        this.activity.getBinding().about.setText(this.localUser.getAbout());
        this.activity.getBinding().location.setText(this.localUser.getLocation());
        loadAvatar();
    }

    private void loadAvatar() {
        ImageUtil.loadFromNetwork(this.localUser.getAvatar(), this.activity.getBinding().avatar);
    }

    private void fetchUser() {
        final Subscription sub = BaseApplication.get()
                .getTokenManager()
                .getUserManager()
                .getUserObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleUserLoaded);

        if (!BaseApplication.get()
                .getTokenManager()
                .getUserManager()
                .getUserObservable()
                .hasValue()) {
            handleNoUser();
        }

        this.subscriptions.add(sub);
    }

    private void handleUserLoaded(final User user) {
        if (user == null) {
            handleNoUser();
            return;
        }
        this.localUser = user;
        updateView();
        fetchUserReputation(user.getTokenId());
    }

   private void handleNoUser() {
        if (this.activity == null) return;

        this.activity.getBinding().name.setText(this.activity.getString(R.string.profile__unknown_name));
        this.activity.getBinding().username.setText("");
        this.activity.getBinding().about.setText("");
        this.activity.getBinding().location.setText("");
        this.activity.getBinding().ratingView.setStars(0);
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
        if (this.activity == null) return;
        final String ratingText = this.activity.getResources().getQuantityString(R.plurals.ratings, reputationScore.getCount());
        final String rating = String.format("%s %s", reputationScore.getCount(), ratingText);
        this.activity.getBinding().reviewCount.setText(rating);

        final double score = reputationScore.getScore() != null ? reputationScore.getScore() : 0;
        final String stringScore = String.valueOf(score);
        this.activity.getBinding().ratingView.setStars(score);
        this.activity.getBinding().reputationScore.setText(stringScore);
        this.activity.getBinding().ratingInfo.setRatingInfo(reputationScore);
    }

    private void handleReputationError(final Throwable throwable) {
        LogUtil.e(getClass(), "Error during reputation fetching " + throwable.getMessage());
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
