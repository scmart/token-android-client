package com.tokenbrowser.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.ActivityAboutBinding;
import com.tokenbrowser.presenter.TransactionOverviewPresenter;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.presenter.factory.TransactionOverviewPresenterFactory;

public class TransactionOverviewActivity extends BasePresenterActivity<TransactionOverviewPresenter, TransactionOverviewActivity> {

    private ActivityAboutBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_transaction_overview);
    }

    public ActivityAboutBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<TransactionOverviewPresenter> getPresenterFactory() {
        return new TransactionOverviewPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull TransactionOverviewPresenter presenter) {}

    @Override
    protected int loaderId() {
        return 5009;
    }
}
