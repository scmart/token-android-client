package com.bakkenbaeck.toshi.view.activity;


import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.bakkenbaeck.toshi.R;
import com.bakkenbaeck.toshi.model.User;
import com.bakkenbaeck.toshi.util.LogUtil;
import com.bakkenbaeck.toshi.view.BaseApplication;
import com.supersonic.mediationsdk.logger.LogListener;
import com.supersonic.mediationsdk.logger.SupersonicError;
import com.supersonic.mediationsdk.logger.SupersonicLogger;
import com.supersonic.mediationsdk.model.Placement;
import com.supersonic.mediationsdk.sdk.RewardedVideoListener;
import com.supersonic.mediationsdk.sdk.Supersonic;
import com.supersonic.mediationsdk.sdk.SupersonicFactory;

import rx.Subscriber;

import static android.R.id.content;

public abstract class AdvertisementActivity extends AppCompatActivity {

    private final String adZone = "DefaultRewardedVideo";
    private final String mAppKey = "527a318d";
    private Supersonic mMediationAgent;
    private Snackbar errorSnackbar;

    abstract void onVideoCompleted();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediationAgent = SupersonicFactory.getInstance();
        mMediationAgent.setLogListener(new LogListener() {
            @Override
            public void onLog (SupersonicLogger.SupersonicTag tag, String message, int logLevel) {
                LogUtil.i(getClass(), message);
            }
        });
        mMediationAgent.setRewardedVideoListener(this.rewardedVideoListener);
        BaseApplication.get().getUserManager().getObservable().subscribe(this.currentUserSubscriber);
    }

    private final Subscriber<User> currentUserSubscriber = new Subscriber<User>() {
        @Override
        public void onCompleted() {}

        @Override
        public void onError(final Throwable e) {}

        @Override
        public void onNext(final User user) {
            this.unsubscribe();
            initAdNetworkWithUserId(user.getId());
        }
    };

    private void initAdNetworkWithUserId(final String userId) {
        mMediationAgent.initRewardedVideo(this, mAppKey, userId);
        if (mMediationAgent.isRewardedVideoAvailable()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMediationAgent.showRewardedVideo(adZone);
                }
            }, 150);
        }
    }

    protected void onResume() {
        super.onResume();
        if (mMediationAgent != null) {
            mMediationAgent.onResume (this);
        }
    }

    protected void onPause() {
        super.onPause();
        if (mMediationAgent != null) {
            mMediationAgent.onPause(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediationAgent != null) {
            mMediationAgent.removeRewardedVideoListener();
        }
    }

    private final RewardedVideoListener rewardedVideoListener = new RewardedVideoListener() {
        @Override
        public void onRewardedVideoInitSuccess() {}

        @Override
        public void onRewardedVideoInitFail(final SupersonicError se) {
            int errorCode =  se.getErrorCode();
            String errorMessage = se.getErrorMessage();
            LogUtil.e(getClass(), "onRewardedVideoInitFail: " + errorMessage + " - " + errorCode);
        }

        @Override
        public void onRewardedVideoShowFail(final SupersonicError se) {
            int errorCode =  se.getErrorCode();
            String errorMessage = se.getErrorMessage();
            LogUtil.e(getClass(), "onRewardedVideoShowFail: " + errorMessage + " - " + errorCode);
        }

        @Override
        public void onRewardedVideoAdOpened() {}

        @Override
        public void onRewardedVideoAdClosed() {}

        @Override
        public void onVideoAvailabilityChanged(final boolean available) {
            if (available) {
                mMediationAgent.showRewardedVideo(adZone);
            } else {
                errorSnackbar = Snackbar.make(findViewById(content), R.string.error__supersonic, Snackbar.LENGTH_INDEFINITE);
                errorSnackbar.getView().setBackgroundColor(ContextCompat.getColor(AdvertisementActivity.this, R.color.generalBg));
                errorSnackbar.show();
            }
        }

        @Override
        public void onVideoStart() {
            if (errorSnackbar != null) {
                errorSnackbar.dismiss();
            }
        }

        @Override
        public void onVideoEnd() {}

        @Override
        public void onRewardedVideoAdRewarded(final Placement placement) {
            onVideoCompleted();
        }
    };
}
