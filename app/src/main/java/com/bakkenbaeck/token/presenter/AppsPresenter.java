package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.network.App;
import com.bakkenbaeck.token.model.network.AppSearch;
import com.bakkenbaeck.token.model.network.Apps;
import com.bakkenbaeck.token.network.DirectoryService;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.activity.ViewAppActivity;
import com.bakkenbaeck.token.view.adapter.RecommendedAppsAdapter;
import com.bakkenbaeck.token.view.adapter.SearchAppAdapter;
import com.bakkenbaeck.token.view.custom.RightSpaceItemDecoration;
import com.bakkenbaeck.token.view.fragment.toplevel.AppsFragment;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit2.Response;
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

        if (firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }

        initView();
        checkIfAppsRequestIsNeeded();
    }

    private void initLongLivingObjects() {
        subscriptions = new CompositeSubscription();
    }

    private void initView() {
        initRecyclerViews();
        initSearchView();
    }

    private void checkIfAppsRequestIsNeeded() {
        if (apps != null) {
            addAppsData(this.apps);
        } else {
            requestAppData();
        }
    }

    private void initRecyclerViews() {
        final int spacing = this.fragment.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);

        final RecyclerView recommendedApps = this.fragment.getBinding().recyclerViewRecommendedApps;
        recommendedApps.setLayoutManager(new LinearLayoutManager(this.fragment.getContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendedApps.addItemDecoration(new RightSpaceItemDecoration(spacing));
        final RecommendedAppsAdapter recommendedAppsAdapter = new RecommendedAppsAdapter(new ArrayList<App>());
        recommendedApps.setAdapter(recommendedAppsAdapter);
        recommendedApps.setNestedScrollingEnabled(false);
        recommendedAppsAdapter.setOnItemClickListener(this::handleAppClicked);

        final RecyclerView filteredApps = this.fragment.getBinding().searchList;
        filteredApps.setLayoutManager(new LinearLayoutManager(this.fragment.getContext()));
        final SearchAppAdapter searchAppAdapter = new SearchAppAdapter(new ArrayList<App>());
        filteredApps.setAdapter(searchAppAdapter);
        searchAppAdapter.setOnItemClickListener(this::handleAppClicked);
    }

    private void handleAppClicked(final App app) {
        final Intent intent = new Intent(this.fragment.getContext(), ViewAppActivity.class)
                .putExtra(ViewAppActivity.APP, app);
        this.fragment.getContext().startActivity(intent);
    }

    private void initSearchView() {
        final Subscription sub = RxTextView.textChanges(this.fragment.getBinding().search)
                .skip(1)
                .debounce(400, TimeUnit.MILLISECONDS)
                .map(CharSequence::toString)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(searchString -> updateViewState())
                .flatMap(this::runSearchQuery)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleSearchResponse, this::handleSearchErrorResponse);

        updateViewState();
        subscriptions.add(sub);
    }

    private Observable<Response<AppSearch>> runSearchQuery(final String searchString) {
        return DirectoryService
                .getApi()
                .searchApps(searchString)
                .subscribeOn(Schedulers.io());
    }

    private void handleSearchResponse(final Response<AppSearch> response) {
        if (response.code() == 200) {
            addAppsToRecyclerView(response.body().getResults());
        } else {
            LogUtil.e(getClass(), response.message());
        }
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
        final Subscription sub = DirectoryService
                .getApi()
                .getApps()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleRecommendedAppsResponse, this::handleRecommendedAppsErrorResponse);

        subscriptions.add(sub);
    }

    private void handleRecommendedAppsResponse(final Response<Apps> response) {
        if (response.code() == 200) {
            final List<App> apps = response.body().getApps();
            AppsPresenter.this.apps = apps;
            addAppsData(apps);
        } else {
            LogUtil.e(getClass(), response.message());
        }
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
        if (this.subscriptions != null) {
            this.subscriptions.unsubscribe();
            this.subscriptions = null;
        }

        this.fragment = null;
    }
}
