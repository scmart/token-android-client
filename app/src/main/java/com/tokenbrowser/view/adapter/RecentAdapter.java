package com.tokenbrowser.view.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.model.local.Conversation;
import com.tokenbrowser.model.local.SofaMessage;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.sofa.Message;
import com.tokenbrowser.model.sofa.Payment;
import com.tokenbrowser.model.sofa.PaymentRequest;
import com.tokenbrowser.model.sofa.SofaAdapters;
import com.tokenbrowser.model.sofa.SofaType;
import com.tokenbrowser.R;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;
import com.tokenbrowser.view.adapter.viewholder.ClickableViewHolder;
import com.tokenbrowser.view.adapter.viewholder.ConversationViewHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RecentAdapter extends RecyclerView.Adapter<ConversationViewHolder> implements ClickableViewHolder.OnClickListener {

    private final SofaAdapters adapters;
    private List<Conversation> conversations;
    private OnItemClickListener<Conversation> onItemClickListener;

    public RecentAdapter() {
        this.adapters = new SofaAdapters();
        this.conversations = new ArrayList<>(0);
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

    public void setConversations(final List<Conversation> conversations) {
        this.conversations = conversations;
        notifyDataSetChanged();
    }

    public RecentAdapter setOnItemClickListener(final OnItemClickListener<Conversation> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        return this;
    }

    public void updateConversation(final Conversation conversation) {
        final int position = this.conversations.indexOf(conversation);
        if (position == -1) {
            this.conversations.add(0, conversation);
            notifyItemInserted(0);
            return;
        }

        this.conversations.set(position, conversation);
        notifyItemChanged(position);
    }

    private String formatLastMessage(final SofaMessage sofaMessage) {
        final User localUser = getCurrentLocalUser();

        try {
            switch (sofaMessage.getType()) {
                case SofaType.PLAIN_TEXT: {
                    final Message message = this.adapters.messageFrom(sofaMessage.getPayload());
                    return message.getBody();
                }

                case SofaType.PAYMENT: {
                    final Payment payment = this.adapters.paymentFrom(sofaMessage.getPayload());
                    return payment.toUserVisibleString(sofaMessage.isSentBy(localUser));
                }

                case SofaType.PAYMENT_REQUEST: {
                    final PaymentRequest request = this.adapters.txRequestFrom(sofaMessage.getPayload());
                    return request.toUserVisibleString(sofaMessage.isSentBy(localUser));
                }
                case SofaType.COMMAND_REQUEST:
                case SofaType.INIT_REQUEST:
                case SofaType.INIT:
                case SofaType.UNKNOWN:
                    return "";
            }
        } catch (final IOException ex) {
            LogUtil.error(getClass(), "Error parsing SofaMessage. " + ex);
        }

        return "";
    }

    private User getCurrentLocalUser() {
        // Yes, this blocks. But realistically, a value should be always ready for returning.
        return BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .getCurrentUser()
                .toBlocking()
                .value();
    }
}
