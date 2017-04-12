package com.tokenbrowser.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.ActivityChooseContactBinding;
import com.tokenbrowser.presenter.ChooseContactPresenter;
import com.tokenbrowser.presenter.factory.ChooseContactsPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;

public class ChooseContactsActivity extends BasePresenterActivity<ChooseContactPresenter, ChooseContactsActivity> {

    public static final String VIEW_TYPE = "view_type";

    private ChooseContactPresenter presenter;
    private ActivityChooseContactBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_choose_contact);
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
    protected int loaderId() {
        return 5002;
    }
}