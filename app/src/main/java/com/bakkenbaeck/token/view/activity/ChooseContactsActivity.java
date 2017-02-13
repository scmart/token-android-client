package com.bakkenbaeck.token.view.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.ActivityChooseContactBinding;
import com.bakkenbaeck.token.model.local.ActivityResultHolder;
import com.bakkenbaeck.token.presenter.ChooseContactPresenter;
import com.bakkenbaeck.token.presenter.factory.ChooseContactsPresenterFactory;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;

public class ChooseContactsActivity extends BasePresenterActivity<ChooseContactPresenter, ChooseContactsActivity> {

    public static final String VIEW_TYPE = "view_type";

    private ChooseContactPresenter presenter;
    private ActivityChooseContactBinding binding;
    private ActivityResultHolder resultHolder;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_choose_contact);
    }

    public final ActivityChooseContactBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<ChooseContactPresenter> getPresenterFactory() {
        return new ChooseContactsPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull ChooseContactPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        this.resultHolder = new ActivityResultHolder(requestCode, resultCode, data);
        tryProcessResultHolder();
    }

    private void tryProcessResultHolder() {
        if (this.presenter == null || this.resultHolder == null) {
            return;
        }

        this.presenter.handleActivityResult(this.resultHolder);
        this.resultHolder = null;
    }

    @Override
    protected int loaderId() {
        return 5002;
    }
}