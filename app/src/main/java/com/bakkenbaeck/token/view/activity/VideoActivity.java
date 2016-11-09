package com.bakkenbaeck.token.view.activity;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.ActivityVideoBinding;
import com.bakkenbaeck.token.presenter.PresenterLoader;
import com.bakkenbaeck.token.presenter.VideoPresenter;
import com.bakkenbaeck.token.presenter.factory.VideoPresenterFactory;

import static android.R.id.content;

public class VideoActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<VideoPresenter> {

    private static final int UNIQUE_ACTIVITY_ID = 102;
    private ActivityVideoBinding binding;
    private VideoPresenter presenter;
    private Snackbar errorSnackbar;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        getSupportLoaderManager().initLoader(UNIQUE_ACTIVITY_ID, null, this);
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_video);
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

    public void showNoAdsSnackbar() {
        errorSnackbar = Snackbar.make(findViewById(content), R.string.error__no_more_ads_available, Snackbar.LENGTH_INDEFINITE);
        errorSnackbar.show();
    }

    public void hideErrorSnackbar() {
        if (errorSnackbar != null) {
            errorSnackbar.dismiss();
        }
    }

    protected void onResume() {
        super.onResume();
        if (this.presenter != null) {
            this.presenter.onResume(this);
        }
    }

    protected void onPause() {
        super.onPause();
        if (this.presenter != null) {
            this.presenter.onPause(this);
        }
    }
}
