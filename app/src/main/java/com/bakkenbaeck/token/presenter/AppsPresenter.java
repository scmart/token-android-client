package com.bakkenbaeck.token.presenter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.adapter.RecommendedAppsAdapter;
import com.bakkenbaeck.token.view.custom.RightSpaceItemDecoration;
import com.bakkenbaeck.token.view.fragment.toplevel.AppsFragment;

import java.util.ArrayList;

public class AppsPresenter implements Presenter<AppsFragment>{

    private AppsFragment fragment;

    @Override
    public void onViewAttached(AppsFragment view) {
        this.fragment = view;
        initRecyclerViews();
    }

    private void initRecyclerViews() {
        final RecyclerView recommendedApps = this.fragment.getBinding().recyclerViewRecommendedApps;
        final int spacing = this.fragment.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        recommendedApps.setLayoutManager(new LinearLayoutManager(this.fragment.getContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendedApps.addItemDecoration(new RightSpaceItemDecoration(spacing));
        recommendedApps.setAdapter(new RecommendedAppsAdapter(new ArrayList<String>()));
        recommendedApps.setNestedScrollingEnabled(false);
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
