package com.bakkenbaeck.token.view.activity;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.bakkenbaeck.token.databinding.ActivityMainBinding;
import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.presenter.MainPresenter;
import com.bakkenbaeck.token.presenter.PresenterLoader;
import com.bakkenbaeck.token.presenter.factory.MainPresenterFactory;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<MainPresenter> {

    private static final int UNIQUE_ACTIVITY_ID = 101;
    private MainPresenter presenter;
    private ActivityMainBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        getSupportLoaderManager().initLoader(UNIQUE_ACTIVITY_ID, null, this);
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    }

    @Override
    public final void onStart() {
        super.onStart();
        this.presenter.onViewAttached(this);
    }

    @Override
    public final void onStop() {
        super.onStop();
        this.presenter.onViewDetached();
    }

    @Override
    public Loader<MainPresenter> onCreateLoader(final int id, final Bundle args) {
        return new PresenterLoader<>(this, new MainPresenterFactory());
    }

    @Override
    public void onLoadFinished(final Loader<MainPresenter> loader, final MainPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onLoaderReset(final Loader<MainPresenter> loader) {
        this.presenter.onViewDestroyed();
        this.presenter = null;
    }

    public final ActivityMainBinding getBinding() {
        return this.binding;
    }
}
