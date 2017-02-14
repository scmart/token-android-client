package com.bakkenbaeck.token.view.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.local.ChatMessage;
import com.bakkenbaeck.token.model.local.Conversation;
import com.bakkenbaeck.token.model.sofa.Message;
import com.bakkenbaeck.token.model.sofa.Payment;
import com.bakkenbaeck.token.model.sofa.PaymentRequest;
import com.bakkenbaeck.token.model.sofa.SofaAdapters;
import com.bakkenbaeck.token.model.sofa.SofaType;
import com.bakkenbaeck.token.presenter.store.ConversationStore;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.adapter.listeners.OnItemClickListener;
import com.bakkenbaeck.token.view.adapter.viewholder.ClickableViewHolder;
import com.bakkenbaeck.token.view.adapter.viewholder.ConversationViewHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmResults;

public class RecentAdapter extends RecyclerView.Adapter<ConversationViewHolder> implements ClickableViewHolder.OnClickListener {

    private final SofaAdapters adapters;
    private List<Conversation> conversations;
    private OnItemClickListener<Conversation> onItemClickListener;

    public RecentAdapter() {
        this.adapters = new SofaAdapters();
        this.conversations = new ArrayList<>(0);
    }

    public RecentAdapter loadAllStoredContacts() {
        new ConversationStore()
                .loadAll()
                .subscribe(new SingleSuccessSubscriber<RealmResults<Conversation>>() {
                    @Override
                    public void onSuccess(final RealmResults<Conversation> conversations) {
                        RecentAdapter.this.conversations = conversations;
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

        final String formattedLatestMessage = formatLastMessage(conversation.getLatestMessage());
        holder.setLatestMessage(formattedLatestMessage);
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

    public RecentAdapter setOnItemClickListener(final OnItemClickListener<Conversation> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }

    public void clear() {
        this.conversations.clear();
        notifyDataSetChanged();
    }

    private String formatLastMessage(final ChatMessage chatMessage) {

        try {
            switch (chatMessage.getType()) {
                case SofaType.PLAIN_TEXT: {
                    final Message message = this.adapters.messageFrom(chatMessage.getPayload());
                    return message.getBody();
                }

                case SofaType.PAYMENT: {
                    final Payment payment = this.adapters.paymentFrom(chatMessage.getPayload());
                    return payment.toUserVisibleString();
                }

                case SofaType.PAYMENT_REQUEST: {
                    final PaymentRequest request = this.adapters.txRequestFrom(chatMessage.getPayload());
                    return request.toUserVisibleString();
                }
            }
        } catch (final IOException ex) {
            LogUtil.error(getClass(), "Error parsing ChatMessage. " + ex);
        }

        return "";
    }
}
