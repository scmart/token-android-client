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
import com.bakkenbaeck.token.presenter.NavigationPresenter;
import com.bakkenbaeck.token.presenter.PresenterLoader;
import com.bakkenbaeck.token.presenter.factory.NavigationPresenterFactory;

public class NavigationFragment extends Fragment implements LoaderManager.LoaderCallbacks<NavigationPresenter> {

    private static final int LOADER_ID = 101;
    private NavigationPresenter presenter;

    public static NavigationFragment newInstance(final int position) {
        final NavigationFragment f = new NavigationFragment();
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
    public Loader<NavigationPresenter> onCreateLoader(final int id, final Bundle args) {
        return new PresenterLoader<>(this.getContext(), new NavigationPresenterFactory());
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
    public void onLoadFinished(final Loader<NavigationPresenter> loader, final NavigationPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onLoaderReset(final Loader<NavigationPresenter> loader) {
        this.presenter.onViewDestroyed();
        this.presenter = null;
    }
}
