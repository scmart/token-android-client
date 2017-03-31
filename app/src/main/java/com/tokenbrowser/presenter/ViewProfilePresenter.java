package com.tokenbrowser.presenter;

import android.view.View;

import com.bumptech.glide.Glide;
import com.tokenbrowser.R;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.network.ReputationScore;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.OnSingleClickListener;
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

        final Subscription sub =
                BaseApplication.get()
                .isConnectedSubject()
                .subscribe(this::handleConnectionChanged);
        this.subscriptions.add(sub);
    }

    private void handleConnectionChanged(final Boolean isConnected) {
        if (this.fragment == null) {
            return;
        }
        
        this.fragment.getBinding().editProfileButton.setEnabled(isConnected);
    }

    private void updateView() {
        if (this.localUser == null || this.fragment == null) {
            return;
        }

        this.fragment.getBinding().name.setText(this.localUser.getDisplayName());
        this.fragment.getBinding().username.setText(this.localUser.getUsername());
        this.fragment.getBinding().about.setText(this.localUser.getAbout());
        this.fragment.getBinding().location.setText(this.localUser.getLocation());
        Glide.with(this.fragment)
                .load(this.localUser.getAvatar())
                .into(this.fragment.getBinding().avatar);
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
        this.localUser = user;
        updateView();
        fetchUserReputation(user.getTokenId());
    }

   private void handleNoUser() {
        if (this.fragment == null) {
            return;
        }

        this.fragment.getBinding().name.setText(this.fragment.getString(R.string.profile__unknown_name));
        this.fragment.getBinding().username.setText("");
        this.fragment.getBinding().about.setText("");
        this.fragment.getBinding().location.setText("");
        this.fragment.getBinding().ratingView.setStars(0);
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
        this.subscriptions.clear();
        this.fragment = null;
    }

    @Override
    public void onDestroyed() {
        this.subscriptions = null;
        this.fragment = null;
    }
}
