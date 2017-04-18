/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.presenter;

import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.util.KeyboardUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.OnSingleClickListener;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.UserSearchActivity;
import com.tokenbrowser.view.activity.ViewUserActivity;
import com.tokenbrowser.view.adapter.ContactsAdapter;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;

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
                .subscribe(
                        users -> this.adapter.setUsers(users),
                        e -> LogUtil.e(getClass(), e.toString()));

        this.subscriptions.add(sub);
    }

    private final OnSingleClickListener handleCloseClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            KeyboardUtil.hideKeyboard(v);
            activity.onBackPressed();
        }
    };

    @Override
    public void onItemClick(final User clickedUser) {
        final Intent intent = new Intent(this.activity, ViewUserActivity.class);
        intent.putExtra(ViewUserActivity.EXTRA__USER_ADDRESS, clickedUser.getTokenId());
        this.activity.startActivity(intent);
        this.activity.finish();
    }

    @Override
    public void onViewDetached() {
        this.subscriptions.clear();
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.subscriptions = null;
        this.adapter = null;
    }
}
