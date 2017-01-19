package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.view.activity.ChatActivity;
import com.bakkenbaeck.token.view.activity.UserSearchActivity;
import com.bakkenbaeck.token.view.adapter.UserAdapter;
import com.bakkenbaeck.token.view.adapter.listeners.OnItemClickListener;
import com.bakkenbaeck.token.view.fragment.children.ContactsListFragment;

public final class ContactsListPresenter implements
        Presenter<ContactsListFragment>,
        OnItemClickListener<User> {

    private ContactsListFragment fragment;
    private boolean firstTimeAttaching = true;
    private UserAdapter adapter;

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
        final RecyclerView recyclerView = this.fragment.getBinding().contacts;
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.fragment.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(this.adapter);

        final DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);

        this.fragment.getBinding().userSearch.setOnClickListener(this.handleUserSearchClicked);
        updateEmptyState();
    }

    private void initLongLivingObjects() {
        this.adapter = new UserAdapter()
                .loadAllStoredContacts()
                .setOnItemClickListener(this);
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
        final Intent intent = new Intent(this.fragment.getActivity(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA__REMOTE_USER, clickedUser);
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
