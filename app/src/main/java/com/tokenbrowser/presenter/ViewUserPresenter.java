package com.tokenbrowser.presenter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.network.ReputationScore;
import com.tokenbrowser.token.R;
import com.tokenbrowser.token.databinding.ActivityScanResultBinding;
import com.tokenbrowser.util.ImageUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.OnSingleClickListener;
import com.tokenbrowser.util.SoundManager;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.ChatActivity;
import com.tokenbrowser.view.activity.ViewUserActivity;

import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public final class ViewUserPresenter implements Presenter<ViewUserActivity> {

    private boolean firstTimeAttached = true;
    private CompositeSubscription subscriptions;
    private ViewUserActivity activity;
    private User scannedUser;

    @Override
    public void onViewAttached(final ViewUserActivity activity) {
        this.activity = activity;
        if (this.firstTimeAttached) {
            this.firstTimeAttached = false;
            initLongLivingObjects();
        }
        initShortLivingObjects();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
        final Intent intent = activity.getIntent();
        final String userAddress = intent.getStringExtra(ViewUserActivity.EXTRA__USER_ADDRESS);
        loadOrFetchUser(userAddress);
        fetchUserReputation(userAddress);
        generateQrCode(userAddress);
    }

    private void generateQrCode(final String address) {
        final Subscription sub = ImageUtil
                .generateQrCodeForWalletAddress(address)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::renderQrCode);

        this.subscriptions.add(sub);
    }

    private void renderQrCode(final Bitmap qrCodeBitmap) {
        if (this.activity == null || this.activity.getBinding() == null) {
            return;
        }
        this.activity.getBinding().qrCodeImage.setAlpha(0.0f);
        this.activity.getBinding().qrCodeImage.setImageBitmap(qrCodeBitmap);
        this.activity.getBinding().qrCodeImage.animate().alpha(1f).setDuration(200).start();
    }

    private void initShortLivingObjects() {
        this.activity.getBinding().closeButton.setOnClickListener((View v) -> this.activity.onBackPressed());
    }

    private void loadOrFetchUser(final String userAddress) {
        final Subscription userSub = BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .getUserFromAddress(userAddress)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleUserLoaded, this::handleUserLoadingFailed);

        this.subscriptions.add(userSub);
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
        final String reviewCount = String.valueOf(reputationScore.getCount());
        final double score = reputationScore.getScore() != null ? reputationScore.getScore() : 0;
        final String stringScore = String.valueOf(score);
        this.activity.getBinding().reviewCount.setText(reviewCount);
        this.activity.getBinding().ratingView.setStars(score);
        this.activity.getBinding().reputationScore.setText(stringScore);
        this.activity.getBinding().ratingInfo.setRatingInfo(reputationScore.getStars());
    }

    private void handleReputationError(final Throwable throwable) {
        LogUtil.e(getClass(), "Error during reputation fetching " + throwable.getMessage());
    }

    private void handleUserLoadingFailed(final Throwable throwable) {
        if (this.activity != null) {
            this.activity.finish();
            Toast.makeText(this.activity, R.string.error_unknown_user, Toast.LENGTH_LONG).show();
        }
    }

    private void handleUserLoaded(final User scannedUser) {
        this.scannedUser = scannedUser;
        final ActivityScanResultBinding binding = this.activity.getBinding();
        binding.title.setText(scannedUser.getDisplayName());
        binding.name.setText(scannedUser.getDisplayName());
        binding.username.setText(scannedUser.getUsername());
        binding.about.setText(scannedUser.getAbout());
        binding.location.setText(scannedUser.getLocation());
        binding.addContactButton.setOnClickListener(handleOnAddContact);
        binding.addContactButton.setEnabled(true);
        binding.messageContactButton.setOnClickListener(this::handleMessageContactButton);
        binding.ratingView.setStars(3.6);
        updateAddContactState();
    }

    private void updateAddContactState() {
        final Subscription sub = isAContact(this.scannedUser).subscribe(this::updateAddContactState);
        this.subscriptions.add(sub);
    }

    private void updateAddContactState(final boolean isAContact) {
        final ToggleButton addContactButton = this.activity.getBinding().addContactButton;
        addContactButton.setChecked(isAContact);
        addContactButton.setSoundEffectsEnabled(isAContact);

        final Drawable checkMark = AppCompatResources.getDrawable(this.activity, R.drawable.ic_done);
        DrawableCompat.setTint(checkMark, this.activity.getResources().getColor(R.color.colorPrimary));
        addContactButton.setCompoundDrawablesWithIntrinsicBounds(isAContact ? checkMark : null, null, null, null);
    }

    private final OnSingleClickListener handleOnAddContact = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            final Subscription sub = isAContact(scannedUser).subscribe(this::handleAddContact);
            subscriptions.add(sub);
        }

        private void handleAddContact(final boolean isAContact) {
            if (isAContact) {
                deleteContact(scannedUser);
            } else {
                saveContact(scannedUser);
                SoundManager.getInstance().playSound(SoundManager.ADD_CONTACT);
            }
            updateAddContactState();
        }
    };

    private Single<Boolean> isAContact(final User user) {
        return BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .isUserAContact(user)
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void deleteContact(final User user) {
        BaseApplication
            .get()
            .getTokenManager()
            .getUserManager()
            .deleteContact(user);
    }

    private void saveContact(final User user) {
        BaseApplication
            .get()
            .getTokenManager()
            .getUserManager()
            .saveContact(user);
    }

    private void handleMessageContactButton(final View view) {
        final Intent intent = new Intent(this.activity, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS, this.scannedUser.getOwnerAddress());
        this.activity.startActivity(intent);
        this.activity.finish();
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        this.subscriptions.clear();
        this.activity = null;
    }
}
