package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.view.activity.UserSearchActivity;
import com.bakkenbaeck.token.view.activity.ViewUserActivity;
import com.bakkenbaeck.token.view.adapter.ContactsAdapter;
import com.bakkenbaeck.token.view.adapter.listeners.OnItemClickListener;
import com.bakkenbaeck.token.view.custom.HorizontalLineDivider;
import com.bakkenbaeck.token.view.fragment.children.ContactsListFragment;

public final class ContactsListPresenter implements
        Presenter<ContactsListFragment>,
        OnItemClickListener<User> {

    private ContactsListFragment fragment;
    private boolean firstTimeAttaching = true;
    private ContactsAdapter adapter;

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
        // Refresh all contacts each time.
        this.adapter.loadAllStoredContacts();

        initRecyclerView();

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
                new HorizontalLineDivider(fragment.getResources().getColor(R.color.divider))
                        .setLeftPadding(dividerLeftPadding);
        recyclerView.addItemDecoration(lineDivider);
    }

    private void initLongLivingObjects() {
        this.adapter = new ContactsAdapter()
                .setOnItemClickListener(this)
                .setOnUpdateListener(this::updateEmptyState);
    }

    @Override
    public void onViewDetached() {
        this.fragment = null;
    }

    @Override
    public void onViewDestroyed() {
        this.fragment = null;
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
}
