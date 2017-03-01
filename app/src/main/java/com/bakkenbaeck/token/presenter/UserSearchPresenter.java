package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.UserSearchActivity;
import com.bakkenbaeck.token.view.activity.ViewUserActivity;
import com.bakkenbaeck.token.view.adapter.ContactsAdapter;
import com.bakkenbaeck.token.view.adapter.listeners.OnItemClickListener;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public final class UserSearchPresenter
        implements
            Presenter<UserSearchActivity>,
            OnItemClickListener<User> {

    private boolean firstTimeAttaching = true;
    private UserSearchActivity activity;
    private ContactsAdapter adapter;
    private CompositeSubscription subscriptions;

    @Override
    public void onViewAttached(final UserSearchActivity activity) {
        this.activity = activity;
        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }

        initShortLivingObjects();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
        this.adapter = new ContactsAdapter()
                .setOnItemClickListener(this);
    }

    private void initShortLivingObjects() {
        initToolbar();
        initRecyclerView();
    }

    private void initToolbar() {
        this.activity.getBinding().closeButton.setOnClickListener(this.handleCloseClicked);

        final Subscription sub = RxTextView
                .textChangeEvents(this.activity.getBinding().userInput)
                .debounce(500, TimeUnit.MILLISECONDS)
                .map(event -> event.text().toString())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::submitQuery);

        this.subscriptions.add(sub);
    }

    private void initRecyclerView() {
        final RecyclerView recyclerView = this.activity.getBinding().searchResults;
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(this.adapter);

        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    private void submitQuery(final String query) {
        if (query.length() < 3) {
            this.adapter.clear();
            return;
        }

        final Subscription sub = BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .searchOnlineUsers(query)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((users -> this.adapter.setUsers(users)));

        this.subscriptions.add(sub);
    }

    private final OnSingleClickListener handleCloseClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            activity.onBackPressed();
        }
    };

    @Override
    public void onItemClick(final User clickedUser) {
        final Intent intent = new Intent(this.activity, ViewUserActivity.class);
        intent.putExtra(ViewUserActivity.EXTRA__USER_ADDRESS, clickedUser.getOwnerAddress());
        this.activity.startActivity(intent);
        this.activity.finish();
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        this.subscriptions.clear();
        this.activity = null;
    }
}
