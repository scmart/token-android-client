package com.tokenbrowser.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.ActivityQrCodeBinding;
import com.tokenbrowser.presenter.QrCodePresenter;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.presenter.factory.QrCodePresenterFactory;

public class QrCodeActivity extends BasePresenterActivity<QrCodePresenter, QrCodeActivity> {

    private ActivityQrCodeBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_qr_code);
    }

    public final ActivityQrCodeBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<QrCodePresenter> getPresenterFactory() {
        return new QrCodePresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull QrCodePresenter presenter) {}

    @Override
    protected int loaderId() {
        return 5008;
    }
}
