package com.bakkenbaeck.token.view.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.local.Conversation;
import com.bakkenbaeck.token.presenter.store.ConversationStore;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.adapter.listeners.OnItemClickListener;
import com.bakkenbaeck.token.view.adapter.viewholder.ClickableViewHolder;
import com.bakkenbaeck.token.view.adapter.viewholder.ConversationViewHolder;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;

public class RecentsAdapter extends RecyclerView.Adapter<ConversationViewHolder> implements ClickableViewHolder.OnClickListener {

    private List<Conversation> conversations;
    private OnItemClickListener<Conversation> onItemClickListener;

    public RecentsAdapter() {
        this.conversations = new ArrayList<>(0);
    }

    public RecentsAdapter loadAllStoredContacts() {
        new ConversationStore()
                .loadAll()
                .subscribe(new SingleSuccessSubscriber<RealmResults<Conversation>>() {
                    @Override
                    public void onSuccess(final RealmResults<Conversation> conversations) {
                        RecentsAdapter.this.conversations = conversations;
                        notifyDataSetChanged();
                    }
                });
        return this;
    }

    @Override
    public ConversationViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__recent, parent, false);
        return new ConversationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ConversationViewHolder holder, final int position) {
        final Conversation conversation = this.conversations.get(position);
        holder.setConversation(conversation);
        holder.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return this.conversations.size();
    }

    @Override
    public void onClick(final int position) {
        if (this.onItemClickListener == null) {
            return;
        }

        final Conversation clickedConversation = conversations.get(position);
        this.onItemClickListener.onItemClick(clickedConversation);
    }

    public RecentsAdapter setOnItemClickListener(final OnItemClickListener<Conversation> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }

    public void clear() {
        this.conversations.clear();
        notifyDataSetChanged();
    }
}
