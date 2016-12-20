package com.bakkenbaeck.token.view.fragment.toplevel;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.FragmentScannerBinding;
import com.bakkenbaeck.token.presenter.ScannerPresenter;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;
import com.bakkenbaeck.token.presenter.factory.ScannerPresenterFactory;
import com.bakkenbaeck.token.view.fragment.BasePresenterFragment;

public class ScannerFragment extends BasePresenterFragment<ScannerPresenter, ScannerFragment> {
    private ScannerPresenter presenter;
    private FragmentScannerBinding binding;

    public static Fragment newInstance(final CharSequence title) {
        final ScannerFragment f = new ScannerFragment();
        final Bundle b = new Bundle();
        b.putCharSequence("title", title);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle inState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_scanner, container, false);
        return binding.getRoot();
    }

    public FragmentScannerBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<ScannerPresenter> getPresenterFactory() {
        return new ScannerPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final ScannerPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    protected int loaderId() {
        return 2;
    }
}