package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bakkenbaeck.token.BuildConfig;
import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.ProfileActivity;
import com.bakkenbaeck.token.view.adapter.SettingsAdapter;
import com.bakkenbaeck.token.view.fragment.children.SettingsFragment;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class SettingsPresenter implements
        Presenter<SettingsFragment> {

    private User localUser;
    private SettingsFragment fragment;
    private OnNextSubscriber<User> handleUserLoaded;

    @Override
    public void onViewAttached(final SettingsFragment fragment) {
        this.fragment = fragment;

        addListeners();
        setVersionName();
        initRecyclerView();
        updateUi();
    }

    private void addListeners() {
        generateUserLoadedHandler();
        BaseApplication.get()
                .getTokenManager()
                .getUserManager()
                .getUserObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleUserLoaded);
    }

    private void setVersionName() {
        final String versionName = BuildConfig.VERSION_NAME;
        this.fragment.getBinding().version.setText(versionName);
    }

    private void initRecyclerView() {
        final SettingsAdapter adapter = new SettingsAdapter();
        final RecyclerView recyclerView = this.fragment.getBinding().settings;
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.fragment.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void generateUserLoadedHandler() {
        this.handleUserLoaded = new OnNextSubscriber<User>() {
            @Override
            public void onNext(final User user) {
                SettingsPresenter.this.localUser = user;
                updateUi();
            }
        };
    }

    private void updateUi() {
        if (this.localUser != null) {
            this.fragment.getBinding().name.setText(this.localUser.getDisplayName());
            this.fragment.getBinding().username.setText(this.localUser.getUsername());
            this.fragment.getBinding().ratingView.setStars(3.6);
        }

        this.fragment.getBinding().myProfileCard.setOnClickListener(this.handleMyProfileClicked);
    }

    private final OnSingleClickListener handleMyProfileClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            final Intent intent = new Intent(fragment.getActivity(), ProfileActivity.class);
            fragment.startActivity(intent);
        }
    };

    @Override
    public void onViewDetached() {
        destroy();
    }

    @Override
    public void onViewDestroyed() {
        destroy();
    }

    private void destroy() {
        this.handleUserLoaded.unsubscribe();
        this.fragment = null;
    }
}
