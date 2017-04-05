package com.tokenbrowser.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.ActivityViewProfileBinding;
import com.tokenbrowser.presenter.ViewProfilePresenter;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.presenter.factory.ViewProfilePresenterFactory;

public class ViewProfileActivity extends BasePresenterActivity<ViewProfilePresenter, ViewProfileActivity> {

    private ActivityViewProfileBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_view_profile);
    }

    public ActivityViewProfileBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<ViewProfilePresenter> getPresenterFactory() {
        return new ViewProfilePresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final ViewProfilePresenter presenter) {}

    @Override
    protected int loaderId() {
        return 5002;
    }
}
