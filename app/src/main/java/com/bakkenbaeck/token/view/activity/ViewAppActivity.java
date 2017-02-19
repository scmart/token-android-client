package com.bakkenbaeck.token.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.ActivityViewAppBinding;
import com.bakkenbaeck.token.presenter.ViewAppPresenter;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;
import com.bakkenbaeck.token.presenter.factory.ViewAppPresenterFactory;

public class ViewAppActivity extends BasePresenterActivity<ViewAppPresenter, ViewAppActivity> {

    public static final String APP = "app";

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
