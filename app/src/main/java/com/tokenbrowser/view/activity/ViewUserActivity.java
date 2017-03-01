package com.tokenbrowser.view.activity;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.token.R;
import com.tokenbrowser.token.databinding.ActivityScanResultBinding;
import com.tokenbrowser.presenter.ViewUserPresenter;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.presenter.factory.ViewUserPresenterFactory;

public class ViewUserActivity extends BasePresenterActivity<ViewUserPresenter, ViewUserActivity> {
    public static final String EXTRA__USER_ADDRESS = "extra_user_address";

    private ActivityScanResultBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_scan_result);
    }

    public ActivityScanResultBinding getBinding() {
        return this.binding;
    }


    @NonNull
    @Override
    protected PresenterFactory<ViewUserPresenter> getPresenterFactory() {
        return new ViewUserPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final ViewUserPresenter presenter) {

    }

    @Override
    protected int loaderId() {
        return 3001;
    }
}
