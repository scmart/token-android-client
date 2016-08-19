package com.bakkenbaeck.toshi.view.activity;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bakkenbaeck.toshi.R;
import com.bakkenbaeck.toshi.databinding.ActivityWithdrawBinding;
import com.bakkenbaeck.toshi.model.ActivityResultHolder;
import com.bakkenbaeck.toshi.presenter.PresenterLoader;
import com.bakkenbaeck.toshi.presenter.WithdrawPresenter;
import com.bakkenbaeck.toshi.presenter.factory.WithdrawPresenterFactory;

public class WithdrawActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<WithdrawPresenter> {

    private static final int UNIQUE_ACTIVITY_ID = 104;
    private WithdrawPresenter presenter;
    private ActivityWithdrawBinding binding;
    private ActivityResultHolder activityResultHolder;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        getSupportLoaderManager().initLoader(UNIQUE_ACTIVITY_ID, null, this);
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_withdraw);

        final RecyclerView.LayoutManager recyclerLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        this.binding.previousWallets.setLayoutManager(recyclerLayoutManager);
    }

    public final ActivityWithdrawBinding getBinding() {
        return this.binding;
    }

    @Override
    public Loader<WithdrawPresenter> onCreateLoader(final int id, final Bundle args) {
        return new PresenterLoader<>(this, new WithdrawPresenterFactory());
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        this.activityResultHolder = new ActivityResultHolder(requestCode, resultCode, data);
    }

    @Override
    public final void onStart() {
        super.onStart();
        this.presenter.onViewAttached(this);
        if (this.activityResultHolder != null) {
            this.presenter.handleActivityResult(this.activityResultHolder);
            this.activityResultHolder = null;
        }
    }

    @Override
    public final void onStop() {
        super.onStop();
        this.presenter.onViewDetached();
    }

    @Override
    public void onLoadFinished(final Loader<WithdrawPresenter> loader, final WithdrawPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onLoaderReset(final Loader<WithdrawPresenter> loader) {
        this.presenter.onViewDestroyed();
        this.presenter = null;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        this.overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
        return true;
    }
}
