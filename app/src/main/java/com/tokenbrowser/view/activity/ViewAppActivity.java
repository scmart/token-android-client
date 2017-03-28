package com.tokenbrowser.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.ActivityViewAppBinding;
import com.tokenbrowser.presenter.ViewAppPresenter;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.presenter.factory.ViewAppPresenterFactory;

public class ViewAppActivity extends BasePresenterActivity<ViewAppPresenter, ViewAppActivity> {

    public static final String APP_OWNER_ADDRESS = "app_owner_address";

    private ActivityViewAppBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_view_app);
    }

    public ActivityViewAppBinding getBinding() {
        return this.binding;
    }


    @NonNull
    @Override
    protected PresenterFactory<ViewAppPresenter> getPresenterFactory() {
        return new ViewAppPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final ViewAppPresenter presenter) {

    }

    @Override
    protected int loaderId() {
        return 2001;
    }
}
