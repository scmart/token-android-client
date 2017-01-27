package com.bakkenbaeck.token.presenter;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bakkenbaeck.token.view.adapter.AppListAdapter;
import com.bakkenbaeck.token.view.fragment.toplevel.HomeFragment;

import java.util.ArrayList;
import java.util.List;

public class HomePresenter implements Presenter<HomeFragment> {

    private HomeFragment fragment;

    @Override
    public void onViewAttached(HomeFragment view) {
        this.fragment = view;
        initView();
    }

    private void initView() {
        final RecyclerView rv = this.fragment.getBinding().appList;
        rv.setLayoutManager(new GridLayoutManager(this.fragment.getContext(), 4));

        final List<String> apps = new ArrayList<>();
        apps.add("Draw something");
        apps.add("Token Portfolio");
        apps.add("Perfect GIF");
        apps.add("Local Trade");
        apps.add("Trade ");
        apps.add("Tickets");


        rv.setAdapter(new AppListAdapter(apps));
    }

    @Override
    public void onViewDetached() {
        this.fragment = null;
    }

    @Override
    public void onViewDestroyed() {
        this.fragment = null;
    }
}
