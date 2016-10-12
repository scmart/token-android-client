package com.bakkenbaeck.token.presenter;


import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.VideoActivity;
import com.supersonic.mediationsdk.logger.LogListener;
import com.supersonic.mediationsdk.logger.SupersonicError;
import com.supersonic.mediationsdk.logger.SupersonicLogger;
import com.supersonic.mediationsdk.model.Placement;
import com.supersonic.mediationsdk.sdk.RewardedVideoListener;
import com.supersonic.mediationsdk.sdk.Supersonic;
import com.supersonic.mediationsdk.sdk.SupersonicFactory;

public class VideoPresenter implements Presenter<VideoActivity> {

    public final static String INTENT_CLICKED_POSITION = "clickedPosition";
    private final String adZone = "DefaultRewardedVideo";
    private final String mAppKey = "527a318d";

    private VideoActivity activity;
    private Supersonic mMediationAgent;
    private boolean firstTimeAttached = true;
    private boolean startedPlaying = false;
    private boolean videoSuccessfullyViewed = false;
    private Intent callingIntent;

    @Override
    public void onViewAttached(final VideoActivity activity) {
        this.activity = activity;
        this.mMediationAgent = SupersonicFactory.getInstance();
        if (this.firstTimeAttached) {
            this.firstTimeAttached = false;
            this.callingIntent = activity.getIntent();
            mMediationAgent.setLogListener(new LogListener() {
                @Override
                public void onLog (SupersonicLogger.SupersonicTag tag, String message, int logLevel) {
                    LogUtil.i(getClass(), message);
                }
            });
            BaseApplication.get().getUserManager().getObservable().subscribe(this.currentUserSubscriber);
        }
        if (this.videoSuccessfullyViewed) {
            endActivityWithSuccess();
        } else {
            this.mMediationAgent.setRewardedVideoListener(this.rewardedVideoListener);
        }
    }

    private void endActivityWithSuccess() {
         if (this.videoSuccessfullyViewed) {
            this.activity.setResult(Activity.RESULT_OK, this.callingIntent);
            this.activity.finish();
            this.videoSuccessfullyViewed = false;
        }
    }

    private final OnNextSubscriber<User> currentUserSubscriber = new OnNextSubscriber<User>() {
        @Override
        public void onNext(final User user) {
            this.unsubscribe();
            initAdNetworkWithUserId(user.getId());
        }
    };

    private void initAdNetworkWithUserId(final String userId) {
        mMediationAgent.initRewardedVideo(this.activity, mAppKey, userId);
        if (mMediationAgent.isRewardedVideoAvailable()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMediationAgent.showRewardedVideo(adZone);
                }
            }, 150);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!startedPlaying && activity != null) {
                    activity.showErrorSnackbar();
                }
            }
        }, 10000);
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
                activity.showErrorSnackbar();
            }
        }

        @Override
        public void onVideoStart() {
            startedPlaying = true;
            activity.hideErrorSnackbar();
        }

        @Override
        public void onVideoEnd() {}

        @Override
        public void onRewardedVideoAdRewarded(final Placement placement) {
            onVideoCompleted();
        }
    };

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        this.activity = null;
        this.mMediationAgent.removeRewardedVideoListener();
    }

    public void onVideoCompleted() {
        this.videoSuccessfullyViewed = true;
    }

    public void onResume(final VideoActivity videoActivity) {
        this.mMediationAgent.onResume(videoActivity);
    }

    public void onPause(final VideoActivity videoActivity) {
        this.mMediationAgent.onPause(videoActivity);
    }
}
