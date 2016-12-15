package com.bakkenbaeck.token.view.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.presenter.PlaceholderPresenter;
import com.bakkenbaeck.token.presenter.factory.PlaceholderPresenterFactory;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;

public class PlaceholderFragment extends BasePresenterFragment<PlaceholderPresenter, PlaceholderFragment> {

    private PlaceholderPresenter presenter;

    public static PlaceholderFragment newInstance(final int position) {
        final PlaceholderFragment f = new PlaceholderFragment();
        final Bundle args = new Bundle();
        args.putInt("pos", position);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, final @Nullable Bundle inState) {
        final int position = getArguments().getInt("pos", 100);
        final View v =  inflater.inflate(R.layout.fragment_qr, container, false);
        ((TextView)v.findViewById(R.id.qrCodeText)).setText(String.valueOf(position));
        return v;
    }

    @NonNull
    @Override
    protected PresenterFactory<PlaceholderPresenter> getPresenterFactory() {
        return new PlaceholderPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final PlaceholderPresenter presenter) {
        this.presenter = presenter;
    }
}
