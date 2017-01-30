package com.bakkenbaeck.token.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.ActivityAmountBinding;
import com.bakkenbaeck.token.presenter.AmountPresenter;
import com.bakkenbaeck.token.presenter.factory.AmountPresenterFactory;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;

public class AmountActivity extends BasePresenterActivity<AmountPresenter, AmountActivity> {

    private ActivityAmountBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_amount);
    }

    public ActivityAmountBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<AmountPresenter> getPresenterFactory() {
        return new AmountPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull AmountPresenter presenter) {

    }

    @Override
    protected int loaderId() {
        return 8001;
    }
}
