package com.bakkenbaeck.token.view.fragment.children;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.FragmentQrBinding;
import com.bakkenbaeck.token.presenter.QrPresenter;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;
import com.bakkenbaeck.token.presenter.factory.QrPresenterFactory;
import com.bakkenbaeck.token.view.fragment.BasePresenterFragment;

public class QrFragment extends BasePresenterFragment<QrPresenter, QrFragment> {
    private FragmentQrBinding binding;

    public static QrFragment newInstance() {
        return new QrFragment();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle inState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_qr, container, false);
        return binding.getRoot();
    }

    public FragmentQrBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<QrPresenter> getPresenterFactory() {
        return new QrPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final QrPresenter presenter) {}

    @Override
    protected int loaderId() {
        return 4002;
    }
}
