package com.tokenbrowser.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.ActivityAboutBinding;
import com.tokenbrowser.presenter.AboutPresenter;
import com.tokenbrowser.presenter.factory.AboutPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;

public class AboutActivity extends BasePresenterActivity<AboutPresenter, AboutActivity> {

    private ActivityAboutBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about);
    }

    public ActivityAboutBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<AboutPresenter> getPresenterFactory() {
        return new AboutPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull AboutPresenter presenter) {}

    @Override
    protected int loaderId() {
        return 5001;
    }
}
