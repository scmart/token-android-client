package com.tokenbrowser.presenter;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tokenbrowser.model.local.User;
import com.tokenbrowser.token.R;
import com.tokenbrowser.util.OnSingleClickListener;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.UserSearchActivity;
import com.tokenbrowser.view.activity.ViewUserActivity;
import com.tokenbrowser.view.adapter.ContactsAdapter;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;
import com.tokenbrowser.view.custom.HorizontalLineDivider;
import com.tokenbrowser.view.fragment.children.ContactsListFragment;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public final class ContactsListPresenter implements
        Presenter<ContactsListFragment>,
        OnItemClickListener<User> {

    private ContactsListFragment fragment;
    private boolean firstTimeAttaching = true;
    private ContactsAdapter adapter;
    private CompositeSubscription subscriptions;

    @Override
    public void onViewAttached(final ContactsListFragment fragment) {
        this.fragment = fragment;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }
        initShortLivingObjects();
    }

    private void initShortLivingObjects() {
        initRecyclerView();
        loadContacts();
        this.fragment.getBinding().userSearch.setOnClickListener(this.handleUserSearchClicked);
        updateEmptyState();
    }

    private void initRecyclerView() {
        final RecyclerView recyclerView = this.fragment.getBinding().contacts;
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.fragment.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(this.adapter);

        final int dividerLeftPadding = fragment.getResources().getDimensionPixelSize(R.dimen.avatar_size_small)
                + fragment.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        final HorizontalLineDivider lineDivider =
                new HorizontalLineDivider(ContextCompat.getColor(this.fragment.getContext(), R.color.divider))
                        .setLeftPadding(dividerLeftPadding);
        recyclerView.addItemDecoration(lineDivider);
    }

    private void loadContacts() {
        final Subscription sub = BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .loadAllContacts()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(contacts -> this.adapter.mapContactsToUsers(contacts));

        this.subscriptions.add(sub);
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
        this.adapter = new ContactsAdapter()
                .setOnItemClickListener(this)
                .setOnUpdateListener(this::updateEmptyState);
    }

    @Override
    public void onItemClick(final User clickedUser) {
        final Intent intent = new Intent(this.fragment.getActivity(), ViewUserActivity.class);
        intent.putExtra(ViewUserActivity.EXTRA__USER_ADDRESS, clickedUser.getOwnerAddress());
        this.fragment.startActivity(intent);
    }

    private void updateEmptyState() {
        // Hide empty state if we have some content
        final boolean showingEmptyState = this.fragment.getBinding().emptyStateSwitcher.getCurrentView().getId() == this.fragment.getBinding().emptyState.getId();
        final boolean shouldShowEmptyState = this.adapter.getItemCount() == 0;

        if (shouldShowEmptyState && !showingEmptyState) {
            this.fragment.getBinding().emptyStateSwitcher.showPrevious();
        } else if (!shouldShowEmptyState && showingEmptyState) {
            this.fragment.getBinding().emptyStateSwitcher.showNext();
        }
    }

    private final OnSingleClickListener handleUserSearchClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            final Intent intent = new Intent(fragment.getActivity(), UserSearchActivity.class);
            fragment.startActivity(intent);
        }
    };

    @Override
    public void onViewDetached() {
        this.fragment = null;
    }

    @Override
    public void onViewDestroyed() {
        this.subscriptions.clear();
        this.fragment = null;
    }
}
