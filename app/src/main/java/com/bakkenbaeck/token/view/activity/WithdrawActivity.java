package com.bakkenbaeck.token.view.activity;


import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.ActivityWithdrawBinding;
import com.bakkenbaeck.token.model.ActivityResultHolder;
import com.bakkenbaeck.token.presenter.PresenterLoader;
import com.bakkenbaeck.token.presenter.WithdrawPresenter;
import com.bakkenbaeck.token.presenter.factory.WithdrawPresenterFactory;
import com.bakkenbaeck.token.view.Fragment.QrFragment;

public class WithdrawActivity extends AppCompatActivity implements
                                                            LoaderManager.LoaderCallbacks<WithdrawPresenter>{

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
        this.overridePendingTransition(R.anim.enter_fade_in, R.anim.exit_fade_out);
        return true;
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        QrFragment qrFragment = (QrFragment) fm.findFragmentByTag(QrFragment.TAG);

        if(qrFragment != null){
            this.presenter.removeQrFragment();
        }else{
            super.onBackPressed();
            this.overridePendingTransition(R.anim.enter_fade_in, R.anim.exit_fade_out);
            finish();
        }
    }
}
