package com.bakkenbaeck.token.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.ActivityTrustedFriendsBinding;
import com.bakkenbaeck.token.presenter.TrustedFriendsPresenter;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;
import com.bakkenbaeck.token.presenter.factory.TrustedFriendsPresenterFactory;

public class TrustedFriendsActivity extends BasePresenterActivity<TrustedFriendsPresenter, TrustedFriendsActivity> {

    private ActivityTrustedFriendsBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_trusted_friends);
    }

    public ActivityTrustedFriendsBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<TrustedFriendsPresenter> getPresenterFactory() {
        return new TrustedFriendsPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final TrustedFriendsPresenter presenter) {}

    @Override
    protected int loaderId() {
        return 5004;
    }
}
