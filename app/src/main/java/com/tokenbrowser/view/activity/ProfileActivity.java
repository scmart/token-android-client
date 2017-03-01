package com.tokenbrowser.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.token.R;
import com.tokenbrowser.token.databinding.ActivityTopLevelBinding;
import com.tokenbrowser.presenter.ProfilePresenter;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.presenter.factory.ProfilePresenterFactory;

public class ProfileActivity extends BasePresenterActivity<ProfilePresenter, ProfileActivity> {

    private ActivityTopLevelBinding binding;
    private ProfilePresenter presenter;

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
    protected void onPresenterPrepared(@NonNull final ProfilePresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected int loaderId() {
        return 5002;
    }
}
