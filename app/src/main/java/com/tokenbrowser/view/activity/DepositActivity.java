package com.tokenbrowser.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.token.R;
import com.tokenbrowser.token.databinding.ActivityDepositBinding;
import com.tokenbrowser.presenter.DepositPresenter;
import com.tokenbrowser.presenter.factory.DepositPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;

public class DepositActivity extends BasePresenterActivity<DepositPresenter, DepositActivity> {

    private ActivityDepositBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_deposit);
    }

    public final ActivityDepositBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<DepositPresenter> getPresenterFactory() {
        return new DepositPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull DepositPresenter presenter) {}

    @Override
    protected int loaderId() {
        return 5003;
    }
}
