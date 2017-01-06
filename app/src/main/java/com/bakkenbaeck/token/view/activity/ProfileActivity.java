package com.bakkenbaeck.token.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.ActivityTopLevelBinding;
import com.bakkenbaeck.token.presenter.ProfilePresenter;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;
import com.bakkenbaeck.token.presenter.factory.ProfilePresenterFactory;

public class ProfileActivity extends BasePresenterActivity<ProfilePresenter, ProfileActivity> {

    private ActivityTopLevelBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_top_level);
    }

    public ActivityTopLevelBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<ProfilePresenter> getPresenterFactory() {
        return new ProfilePresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final ProfilePresenter presenter) {}

    @Override
    protected int loaderId() {
        return 5002;
    }
}
