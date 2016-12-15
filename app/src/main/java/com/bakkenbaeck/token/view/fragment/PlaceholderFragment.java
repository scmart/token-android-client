package com.bakkenbaeck.token.view.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.presenter.PlaceholderPresenter;
import com.bakkenbaeck.token.presenter.PresenterLoader;
import com.bakkenbaeck.token.presenter.factory.PlaceholderPresenterFactory;

public class PlaceholderFragment extends Fragment implements LoaderManager.LoaderCallbacks<PlaceholderPresenter> {

    private static final int LOADER_ID = 101;
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

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<PlaceholderPresenter> onCreateLoader(final int id, final Bundle args) {
        return new PresenterLoader<>(this.getContext(), new PlaceholderPresenterFactory());
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onViewAttached(this);
    }

    @Override
    public void onPause() {
        presenter.onViewDetached();
        super.onPause();
    }

    @Override
    public void onLoadFinished(final Loader<PlaceholderPresenter> loader, final PlaceholderPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onLoaderReset(final Loader<PlaceholderPresenter> loader) {
        this.presenter.onViewDestroyed();
        this.presenter = null;
    }
}
