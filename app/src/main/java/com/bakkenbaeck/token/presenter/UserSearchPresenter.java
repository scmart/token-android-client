package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.model.network.UserSearchResults;
import com.bakkenbaeck.token.manager.network.IdService;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.activity.UserSearchActivity;
import com.bakkenbaeck.token.view.activity.ViewUserActivity;
import com.bakkenbaeck.token.view.adapter.ContactsAdapter;
import com.bakkenbaeck.token.view.adapter.listeners.OnItemClickListener;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewTextChangeEvent;

import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public final class UserSearchPresenter
        implements
            Presenter<UserSearchActivity>,
            OnItemClickListener<User> {

    private boolean firstTimeAttaching = true;
    private UserSearchActivity activity;
    private OnNextSubscriber<TextViewTextChangeEvent> handleUserInput;
    private ContactsAdapter adapter;

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
        this.adapter =
                new ContactsAdapter().setOnItemClickListener(this);
    }

    private void initShortLivingObjects() {
        initToolbar();
        initRecyclerView();
    }

    private void initToolbar() {
        generateUserInputHandler();
        this.activity.getBinding().closeButton.setOnClickListener(this.handleCloseClicked);
        RxTextView
                .textChangeEvents(this.activity.getBinding().userInput)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleUserInput);
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

    private void generateUserInputHandler() {
        this.handleUserInput = new OnNextSubscriber<TextViewTextChangeEvent>() {
            @Override
            public void onNext(final TextViewTextChangeEvent textViewTextChangeEvent) {
                submitQuery(textViewTextChangeEvent.text().toString());
            }
        };
    }

    private void submitQuery(final String query) {
        if (query.length() < 3) {
            this.adapter.clear();
            return;
        }

        IdService
                .getApi()
                .searchByUsername(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSuccessSubscriber<UserSearchResults>() {
                    @Override
                    public void onSuccess(final UserSearchResults userSearchResults) {
                        UserSearchPresenter.this.adapter.setUsers(userSearchResults.getResults());
                    }
                });
    }

    private final OnSingleClickListener handleCloseClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            activity.onBackPressed();
        }
    };

    @Override
    public void onViewDetached() {
        this.activity = null;
        this.handleUserInput.unsubscribe();
        this.handleUserInput = null;
    }

    @Override
    public void onViewDestroyed() {
        this.activity = null;
    }

    @Override
    public void onItemClick(final User clickedUser) {
        final Intent intent = new Intent(this.activity, ViewUserActivity.class);
        intent.putExtra(ViewUserActivity.EXTRA__USER_ADDRESS, clickedUser.getOwnerAddress());
        this.activity.startActivity(intent);
        this.activity.finish();
    }
}
