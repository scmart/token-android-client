package com.bakkenbaeck.token.view.fragment.toplevel;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.presenter.HomePresenter;
import com.bakkenbaeck.token.presenter.factory.HomePresenterFactory;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;
import com.bakkenbaeck.token.view.fragment.BasePresenterFragment;

public class HomeFragment extends BasePresenterFragment<HomePresenter, HomeFragment> {

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, final @Nullable Bundle inState) {
        final View v =  inflater.inflate(R.layout.fragment_home, container, false);
        ((TextView)v.findViewById(R.id.title)).setText(getString(R.string.tab_1));
        return v;
    }

    @NonNull
    @Override
    protected PresenterFactory<HomePresenter> getPresenterFactory() {
        return new HomePresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull HomePresenter presenter) {}


    @Override
    protected int loaderId() {
        return 5;
    }
}
