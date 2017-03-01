package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bakkenbaeck.token.model.network.App;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.ViewAppActivity;
import com.bakkenbaeck.token.view.adapter.RecommendedAppsAdapter;
import com.bakkenbaeck.token.view.adapter.SearchAppAdapter;
import com.bakkenbaeck.token.view.fragment.toplevel.AppsFragment;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class AppsPresenter implements Presenter<AppsFragment>{

    private List<App> apps;
    private AppsFragment fragment;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(AppsFragment view) {
        this.fragment = view;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }

        initView();
        checkIfAppsRequestIsNeeded();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
    }

    private void initView() {
        initRecyclerViews();
        initSearchView();
    }

    private void checkIfAppsRequestIsNeeded() {
        if (this.apps != null) {
            addAppsData(this.apps);
        } else {
            requestAppData();
        }
    }

    private void initRecyclerViews() {
        final RecyclerView recommendedApps = this.fragment.getBinding().recyclerViewRecommendedApps;
        recommendedApps.setLayoutManager(new LinearLayoutManager(this.fragment.getContext(), LinearLayoutManager.HORIZONTAL, false));
        final RecommendedAppsAdapter recommendedAppsAdapter = new RecommendedAppsAdapter(new ArrayList<>());
        recommendedApps.setAdapter(recommendedAppsAdapter);
        recommendedApps.setNestedScrollingEnabled(false);
        recommendedAppsAdapter.setOnItemClickListener(this::handleAppClicked);

        final RecyclerView filteredApps = this.fragment.getBinding().searchList;
        filteredApps.setLayoutManager(new LinearLayoutManager(this.fragment.getContext()));
        final SearchAppAdapter searchAppAdapter = new SearchAppAdapter(new ArrayList<>());
        filteredApps.setAdapter(searchAppAdapter);
        searchAppAdapter.setOnItemClickListener(this::handleAppClicked);
    }

    private void handleAppClicked(final App app) {
        final Intent intent = new Intent(this.fragment.getContext(), ViewAppActivity.class)
                .putExtra(ViewAppActivity.APP_OWNER_ADDRESS, app.getOwnerAddress());
        this.fragment.getContext().startActivity(intent);
    }

    private void initSearchView() {
        final Subscription sub = RxTextView.textChanges(this.fragment.getBinding().search)
                .skip(1)
                .debounce(400, TimeUnit.MILLISECONDS)
                .map(CharSequence::toString)
                .doOnNext(searchString -> updateViewState())
                .flatMap(this::runSearchQuery)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::addAppsToRecyclerView, this::handleSearchErrorResponse);

        updateViewState();
        this.subscriptions.add(sub);
    }

    private Observable<List<App>> runSearchQuery(final String searchString) {
        return BaseApplication
                .get()
                .getTokenManager()
                .getAppsManager()
                .searchApps(searchString);
    }

    private void handleSearchErrorResponse(final Throwable throwable) {
        LogUtil.e(getClass(), throwable.getMessage());
    }

    private void updateViewState() {
        final boolean shouldShowSearchResult = this.fragment.getBinding().search.getText().toString().length() > 0;

        if (shouldShowSearchResult) {
            this.fragment.getBinding().searchList.setVisibility(View.VISIBLE);
            this.fragment.getBinding().scrollView.setVisibility(View.GONE);
        } else {
            this.fragment.getBinding().searchList.setVisibility(View.GONE);
            this.fragment.getBinding().scrollView.setVisibility(View.VISIBLE);
        }
    }

    private void addAppsToRecyclerView(final List<App> apps) {
        final SearchAppAdapter searchAdapter = (SearchAppAdapter) this.fragment.getBinding().searchList.getAdapter();
        searchAdapter.addItems(apps);
    }

    private void requestAppData() {
        final Subscription sub =
                BaseApplication
                .get()
                .getTokenManager()
                .getAppsManager()
                .getRecommendedApps()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::addAppsData, this::handleRecommendedAppsErrorResponse);

        this.subscriptions.add(sub);
    }

    private void handleRecommendedAppsErrorResponse(final Throwable throwable) {
        LogUtil.e(getClass(), throwable.getMessage());
    }

    private void addAppsData(final List<App> apps) {
        final RecommendedAppsAdapter adapter = (RecommendedAppsAdapter) this.fragment.getBinding().recyclerViewRecommendedApps.getAdapter();
        adapter.setItems(apps);
    }

    @Override
    public void onViewDetached() {
        this.fragment = null;
    }

    @Override
    public void onViewDestroyed() {
        this.subscriptions.unsubscribe();
        this.fragment = null;
    }
}
