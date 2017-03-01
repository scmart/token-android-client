package com.tokenbrowser.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.token.R;
import com.tokenbrowser.token.databinding.ActivityUserSearchBinding;
import com.tokenbrowser.presenter.UserSearchPresenter;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.presenter.factory.UserSearchPresenterFactory;

public class UserSearchActivity extends BasePresenterActivity<UserSearchPresenter, UserSearchActivity> {

    private ActivityUserSearchBinding binding;
    private UserSearchPresenter presenter;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_user_search);
    }

    public ActivityUserSearchBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<UserSearchPresenter> getPresenterFactory() {
        return new UserSearchPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final UserSearchPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected int loaderId() {
        return 4000;
    }
}
