package com.tokenbrowser.view.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.ActivityChooseContactBinding;
import com.tokenbrowser.model.local.ActivityResultHolder;
import com.tokenbrowser.presenter.ChooseContactPresenter;
import com.tokenbrowser.presenter.factory.ChooseContactsPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;

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