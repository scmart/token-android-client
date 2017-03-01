package com.tokenbrowser.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.token.R;
import com.tokenbrowser.token.databinding.ActivityAmountBinding;
import com.tokenbrowser.presenter.AmountPresenter;
import com.tokenbrowser.presenter.factory.AmountPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;

public class AmountActivity extends BasePresenterActivity<AmountPresenter, AmountActivity> {

    public static final String VIEW_TYPE = "type";
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
    protected void onPresenterPrepared(@NonNull AmountPresenter presenter) {}

    @Override
    protected int loaderId() {
        return 8001;
    }
}