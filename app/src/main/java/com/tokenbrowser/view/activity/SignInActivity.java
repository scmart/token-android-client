package com.tokenbrowser.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.ActivitySignInBinding;
import com.tokenbrowser.presenter.SignInPresenter;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.presenter.factory.SignInPresenterFactory;

public class SignInActivity extends BasePresenterActivity<SignInPresenter, SignInActivity> {

    private ActivitySignInBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_in);
    }

    public final ActivitySignInBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<SignInPresenter> getPresenterFactory() {
        return new SignInPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull SignInPresenter presenter) {}

    @Override
    protected int loaderId() {
        return 1;
    }
}
