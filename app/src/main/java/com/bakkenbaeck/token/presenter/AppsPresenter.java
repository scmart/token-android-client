package com.bakkenbaeck.token.presenter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.network.App;
import com.bakkenbaeck.token.model.network.Apps;
import com.bakkenbaeck.token.network.DirectoryService;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.view.adapter.RecommendedAppsAdapter;
import com.bakkenbaeck.token.view.adapter.SearchAppAdapter;
import com.bakkenbaeck.token.view.custom.RightSpaceItemDecoration;
import com.bakkenbaeck.token.view.fragment.toplevel.AppsFragment;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
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
        final RecyclerView recommendedApps = this.fragment.getBinding().recyclerViewRecommendedApps;
        final int spacing = this.fragment.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        recommendedApps.setLayoutManager(new LinearLayoutManager(this.fragment.getContext(), LinearLayoutManager.HORIZONTAL, false));
        recommendedApps.addItemDecoration(new RightSpaceItemDecoration(spacing));
        recommendedApps.setAdapter(new RecommendedAppsAdapter(new ArrayList<App>()));
        recommendedApps.setNestedScrollingEnabled(false);

        final RecyclerView filteredApps = this.fragment.getBinding().searchList;
        filteredApps.setLayoutManager(new LinearLayoutManager(this.fragment.getContext()));
        filteredApps.setAdapter(new SearchAppAdapter(new ArrayList<App>()));
    }

    private void initSearchView() {
        final Subscription sub = RxTextView.textChanges(this.fragment.getBinding().search)
                .map(new Func1<CharSequence, String>() {
                    @Override
                    public String call(CharSequence charSequence) {
                        return charSequence.toString();
                    }
                })
                .subscribe(new OnNextSubscriber<String>() {
                    @Override
                    public void onNext(String s) {
                        updateViewState();
                        search(s);
                    }
                });

        updateViewState();
        subscriptions.add(sub);
    }

    private void search(final String searchString) {
        if (apps == null) {
            return;
        }

        final Subscription sub = Observable.create(new Observable.OnSubscribe<List<App>>() {
            @Override
            public void call(Subscriber<? super List<App>> subscriber) {
                final List<App> searchList = new ArrayList<>();

                for (App app : apps) {
                    final String appName = app.getDisplayName().toLowerCase();
                    if (appName.contains(searchString)) {
                        searchList.add(app);
                    }
                }

                subscriber.onNext(searchList);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new OnNextSubscriber<List<App>>() {
                    @Override
                    public void onNext(List<App> apps) {
                        addAppsToRecyclerView(apps);
                    }
                });

        subscriptions.add(sub);
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

        subscriptions.add(sub);
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
