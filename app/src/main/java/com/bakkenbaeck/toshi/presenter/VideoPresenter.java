package com.bakkenbaeck.toshi.presenter;


import android.app.Activity;

import com.bakkenbaeck.toshi.view.activity.VideoActivity;

public class VideoPresenter implements Presenter<VideoActivity> {

    public final static String INTENT_CLICKED_POSITION = "clickedPosition";
    private VideoActivity activity;

    private boolean videoSuccessfullyViewed = false;

    @Override
    public void onViewAttached(final VideoActivity activity) {
        this.activity = activity;
        trySetActivityResult();
    }

    private void trySetActivityResult() {
        if (this.videoSuccessfullyViewed) {
            this.activity.setResult(Activity.RESULT_OK, activity.getIntent());
            this.activity.finish();
            this.videoSuccessfullyViewed = false;
        }
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        this.activity = null;
    }

    public void onVideoCompleted() {
        this.videoSuccessfullyViewed = true;
    }
}
