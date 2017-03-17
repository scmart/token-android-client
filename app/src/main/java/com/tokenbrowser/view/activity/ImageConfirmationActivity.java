package com.tokenbrowser.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.presenter.ImageConfirmationPresenter;
import com.tokenbrowser.presenter.factory.ImageConfirmationPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.token.R;
import com.tokenbrowser.token.databinding.ActivityImageConfirmationBinding;

public class ImageConfirmationActivity extends BasePresenterActivity<ImageConfirmationPresenter, ImageConfirmationActivity> {

    private ActivityImageConfirmationBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_image_confirmation);
    }

    public ActivityImageConfirmationBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<ImageConfirmationPresenter> getPresenterFactory() {
        return new ImageConfirmationPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull ImageConfirmationPresenter presenter) {}


    @Override
    protected int loaderId() {
        return 4007;
    }
}
