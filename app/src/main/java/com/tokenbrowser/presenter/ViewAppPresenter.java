package com.tokenbrowser.presenter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.ActivityViewAppBinding;
import com.tokenbrowser.model.local.ActivityResultHolder;
import com.tokenbrowser.model.network.App;
import com.tokenbrowser.model.network.ReputationScore;
import com.tokenbrowser.util.ImageUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.PaymentType;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.AmountActivity;
import com.tokenbrowser.view.activity.ChatActivity;
import com.tokenbrowser.view.activity.ViewAppActivity;

import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ViewAppPresenter implements Presenter<ViewAppActivity> {

    private static final int ETH_PAY_CODE = 2;

    private ViewAppActivity activity;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;
    private App app;
    private String appTokenId;

    @Override
    public void onViewAttached(ViewAppActivity view) {
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
        processIntentData();
        loadApp();
        fetchUserReputation();
        initClickListeners();
    }

    private void processIntentData() {
        this.appTokenId = this.activity.getIntent().getStringExtra(ViewAppActivity.APP_OWNER_ADDRESS);
        if (this.appTokenId == null) {
            Toast.makeText(this.activity, R.string.error__app_loading, Toast.LENGTH_LONG).show();
            this.activity.finish();
        }
    }

    private void loadApp() {
        final Subscription sub =
                getAppById(this.appTokenId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleAppLoaded, this::handleAppLoadingFailed);

        this.subscriptions.add(sub);
    }

    private Single<App> getAppById(final String appId) {
        return BaseApplication
                .get()
                .getTokenManager()
                .getAppsManager()
                .getApp(appId);
    }

    private void handleAppLoaded(final App app) {
        this.app = app;
        if (this.app == null || this.activity == null) return;
        initViewWithAppData();
    }

    private void handleAppLoadingFailed(final Throwable throwable) {
        LogUtil.e(getClass(), "Error during fetching of app " + throwable.getMessage());
        if (this.activity == null) return;
        this.activity.finish();
        Toast.makeText(this.activity, R.string.error__app_loading, Toast.LENGTH_LONG).show();
    }

    private void fetchUserReputation() {
        final Subscription reputationSub = BaseApplication
                .get()
                .getTokenManager()
                .getReputationManager()
                .getReputationScore(this.appTokenId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleReputationResponse, this::handleReputationError);

        this.subscriptions.add(reputationSub);
    }

    private void handleReputationResponse(final ReputationScore reputationScore) {
        final String reviewCount = String.valueOf(reputationScore.getCount());
        final double score = reputationScore.getScore() != null ? reputationScore.getScore() : 0;
        final String stringScore = String.valueOf(score);
        this.activity.getBinding().reviewCount.setText(reviewCount);
        this.activity.getBinding().ratingView.setStars(score);
        this.activity.getBinding().reputationScore.setText(stringScore);
        this.activity.getBinding().ratingInfo.setRatingInfo(reputationScore);
    }

    private void handleReputationError(final Throwable throwable) {
        LogUtil.e(getClass(), "Error during reputation fetching " + throwable.getMessage());
    }

    private void initViewWithAppData() {
        if (this.app == null) {
            return;
        }
        final ActivityViewAppBinding binding = this.activity.getBinding();
        binding.title.setText(this.app.getCustom().getName());
        binding.name.setText(this.app.getCustom().getName());
        binding.username.setText(this.app.getCustom().getName());
        ImageUtil.loadFromNetwork(this.app.getCustom().getAvatar(), binding.avatar);
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(this::handleClosedClicked);
        this.activity.getBinding().messageContactButton.setOnClickListener(this::handleOnMessageClicked);
        this.activity.getBinding().favorite.setOnClickListener(v -> handleFavoriteClicked());
        this.activity.getBinding().pay.setOnClickListener(v -> handlePayClicked());
    }

    private void handleClosedClicked(final View view) {
        this.activity.finish();
    }

    private void handleOnMessageClicked(final View v) {
        if (this.app == null) {
            return;
        }
        final Intent intent = new Intent(this.activity, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS, this.app.getTokenId());
        this.activity.startActivity(intent);
    }

    private void handleFavoriteClicked() {
        Toast.makeText(this.activity, "Not implemented", Toast.LENGTH_SHORT).show();
    }

    private void handlePayClicked() {
        final Intent intent = new Intent(this.activity, AmountActivity.class)
                .putExtra(AmountActivity.VIEW_TYPE, PaymentType.TYPE_SEND);
        this.activity.startActivityForResult(intent, ETH_PAY_CODE);
    }

    public boolean handleActivityResult(final ActivityResultHolder resultHolder) {
        if (resultHolder.getResultCode() != Activity.RESULT_OK || this.activity == null) return false;

        final int requestCode = resultHolder.getRequestCode();
        if (requestCode == ETH_PAY_CODE) {
            goToChatActivityFromPay(resultHolder.getIntent());
        }

        return true;
    }
    private void goToChatActivityFromPay(final Intent payResultIntent) {
        final String ethAmount = payResultIntent.getStringExtra(AmountPresenter.INTENT_EXTRA__ETH_AMOUNT);
        final String appId = this.activity.getIntent().getStringExtra(ViewAppActivity.APP_OWNER_ADDRESS);
        final Intent intent = new Intent(this.activity, ChatActivity.class)
                .putExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS, appId)
                .putExtra(ChatActivity.EXTRA__ETH_AMOUNT, ethAmount)
                .putExtra(ChatActivity.EXTRA__PAYMENT_ACTION, PaymentType.TYPE_SEND);
        this.activity.startActivity(intent);
        this.activity.finish();
    }

    @Override
    public void onViewDetached() {
        this.subscriptions.clear();
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.subscriptions = null;
    }
}
