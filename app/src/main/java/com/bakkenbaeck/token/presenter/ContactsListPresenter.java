package com.bakkenbaeck.token.presenter;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bakkenbaeck.token.model.Contact;
import com.bakkenbaeck.token.view.adapter.ContactsAdapter;
import com.bakkenbaeck.token.view.adapter.listeners.OnItemClickListener;
import com.bakkenbaeck.token.view.fragment.children.ContactsListFragment;

public final class ContactsListPresenter implements
        Presenter<ContactsListFragment>,
        OnItemClickListener<Contact> {

    private ContactsListFragment fragment;
    private boolean firstTimeAttaching = true;
    private ContactsAdapter adapter;
    private OnItemClickListener<Contact> onItemClickListener;

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
    }

    private void initLongLivingObjects() {
        this.adapter = new ContactsAdapter();
        this.adapter.setOnItemClickListener(this);
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
    public void onItemClick(final Contact contact) {
        if (this.onItemClickListener == null) {
            return;
        }

        this.onItemClickListener.onItemClick(contact);
    }

    public void setOnItemClickListener(final OnItemClickListener<Contact> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
