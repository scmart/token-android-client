package com.tokenbrowser.view.activity;

import android.support.annotation.NonNull;

import com.tokenbrowser.presenter.SignOutPresenter;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.presenter.factory.SignOutPresenterFactory;

public class SignOutActivity extends BasePresenterActivity<SignOutPresenter, SignOutActivity> {

    @NonNull
    @Override
    protected PresenterFactory<SignOutPresenter> getPresenterFactory() {
        return new SignOutPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull SignOutPresenter presenter) {}

    @Override
    protected int loaderId() {
        return 7000;
    }
}
