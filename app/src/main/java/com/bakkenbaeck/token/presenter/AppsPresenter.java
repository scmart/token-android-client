package com.bakkenbaeck.token.presenter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.network.App;
import com.bakkenbaeck.token.model.network.Apps;
import com.bakkenbaeck.token.network.DirectoryService;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.view.adapter.RecommendedAppsAdapter;
import com.bakkenbaeck.token.view.custom.RightSpaceItemDecoration;
import com.bakkenbaeck.token.view.fragment.toplevel.AppsFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AppsPresenter implements Presenter<AppsFragment>{

    private List<App> apps;
    private AppsFragment fragment;
    private Subscription directorySubscription;

    @Override
    public void onViewAttached(AppsFragment view) {
        this.fragment = view;

        initView();
        checkIfAppsRequestIsNeeded();
    }

    private void initView() {
        initRecyclerViews();
    }

    private void checkIfAppsRequestIsNeeded() {
        if (apps != null) {
            addAppsData(this.apps);
        } else {
            requestAppData();
        }
    }

    private void initRecyclerViews() {
        final RecyclerView recommendedApps = this.fragment.getBinding().recyclerViewRecommendedApps;
        final int spacing = this.fragment.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        recommendedApps.setLayoutManager(new LinearLayoutManager(this.fragment.getContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendedApps.addItemDecoration(new RightSpaceItemDecoration(spacing));
        recommendedApps.setAdapter(new RecommendedAppsAdapter(new ArrayList<App>()));
        recommendedApps.setNestedScrollingEnabled(false);
    }

    private void requestAppData() {
        directorySubscription = DirectoryService
                .getApi()
                .getApps()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new OnNextSubscriber<Response<Apps>>() {
                    @Override
                    public void onError(Throwable e) {
                        LogUtil.e(getClass(), e.getMessage());
                    }

                    @Override
                    public void onNext(final Response<Apps> response) {
                        if (response.code() == 200) {
                            final List<App> apps = response.body().getApps();
                            AppsPresenter.this.apps = apps;
                            addAppsData(apps);
                        } else {
                            LogUtil.e(getClass(), response.message());
                        }
                    }
                });
    }

    private void addAppsData(final List<App> apps) {
        if (apps.size() == 0) {
            return;
        }

        final RecommendedAppsAdapter adapter = (RecommendedAppsAdapter) this.fragment.getBinding().recyclerViewRecommendedApps.getAdapter();
        adapter.setItems(apps);
    }

    @Override
    public void onViewDetached() {
        this.fragment = null;
    }

    @Override
    public void onViewDestroyed() {
        if (this.directorySubscription != null) {
            this.directorySubscription.unsubscribe();
            this.directorySubscription = null;
        }

        this.fragment = null;
    }
}
