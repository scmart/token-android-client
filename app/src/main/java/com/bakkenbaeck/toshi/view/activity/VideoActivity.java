package com.bakkenbaeck.toshi.view.activity;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.bakkenbaeck.toshi.R;
import com.bakkenbaeck.toshi.databinding.ActivityVideoBinding;
import com.bakkenbaeck.toshi.presenter.PresenterLoader;
import com.bakkenbaeck.toshi.presenter.VideoPresenter;
import com.bakkenbaeck.toshi.presenter.factory.VideoPresenterFactory;

public class VideoActivity extends AdvertisementActivity implements LoaderManager.LoaderCallbacks<VideoPresenter> {

    private static final int UNIQUE_ACTIVITY_ID = 102;
    private ActivityVideoBinding binding;
    private VideoPresenter presenter;

    @Override
    void onVideoCompleted() {
        this.presenter.onVideoCompleted();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        getSupportLoaderManager().initLoader(UNIQUE_ACTIVITY_ID, null, this);
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_video);
    }

    public ActivityVideoBinding getBinding() {
        return this.binding;
    }

    @Override
    public final void onStart() {
        super.onStart();
        this.presenter.onViewAttached(this);
    }

    @Override
    public final void onStop() {
        super.onStop();
        presenter.onViewDetached();
    }

    @Override
    public Loader<VideoPresenter> onCreateLoader(final int id, final Bundle args) {
        return new PresenterLoader<>(this, new VideoPresenterFactory());
    }

    @Override
    public void onLoadFinished(final Loader<VideoPresenter> loader, final VideoPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onLoaderReset(final Loader<VideoPresenter> presenter) {
        this.presenter.onViewDestroyed();
        this.presenter = null;
    }
}
