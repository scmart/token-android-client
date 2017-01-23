package com.bakkenbaeck.token.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.local.ChatMessage;
import com.bakkenbaeck.token.model.sofa.Message;
import com.bakkenbaeck.token.model.sofa.SofaAdapters;
import com.bakkenbaeck.token.model.sofa.SofaType;
import com.bakkenbaeck.token.model.sofa.PaymentRequest;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.adapter.viewholder.LocalPaymentRequestViewHolder;
import com.bakkenbaeck.token.view.adapter.viewholder.TextViewHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public final class  MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatMessage> chatMessages;
    private final SofaAdapters adapters;

    public MessageAdapter() {
        this.chatMessages = new ArrayList<>();
        this.adapters = new SofaAdapters();
    }

    public final void addMessages(final Collection<ChatMessage> chatMessages) {
        this.chatMessages.addAll(chatMessages);
        notifyDataSetChanged();
    }

    public final void addMessage(final ChatMessage chatMessage) {
        this.chatMessages.add(chatMessage);
        notifyItemInserted(this.chatMessages.size() - 1);
    }

    @Override
    public int getItemViewType(final int position) {
        final ChatMessage chatMessage = this.chatMessages.get(position);
        return chatMessage.getType();
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        switch (viewType) {

            case SofaType.PAYMENT_REQUEST: {
                final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__payment_request, parent, false);
                return new LocalPaymentRequestViewHolder(v);
            }

            case SofaType.UNKNOWN:
            case SofaType.PLAIN_TEXT:
            default: {
                final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__text_message, parent, false);
                return new TextViewHolder(v);
            }

        }
    }

    @Override
    public final void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final ChatMessage chatMessage = this.chatMessages.get(position);
        final String requestPayload = chatMessage.getPayload();
        if (requestPayload == null) {
            return;
        }


        switch (holder.getItemViewType()) {

            case SofaType.PLAIN_TEXT: {
                final TextViewHolder vh = (TextViewHolder) holder;
                try {
                    final Message message = this.adapters.messageFrom(requestPayload);
                    vh.setText(message.getBody(), chatMessage.isSentByLocal());
                } catch (final IOException e) {
                    LogUtil.print(getClass(), e.toString());
                }

                break;
            }

            case SofaType.PAYMENT_REQUEST: {
                final LocalPaymentRequestViewHolder vh = (LocalPaymentRequestViewHolder) holder;

                try {
                    final PaymentRequest request = this.adapters.txRequestFrom(requestPayload);
                    vh.setTxRequest(request, chatMessage.isSentByLocal());
                } catch (final IOException e) {
                    LogUtil.print(getClass(), e.toString());
                }

                break;
            }
        }
    }

    @Override
    public final int getItemCount() {
        return this.chatMessages.size();
    }
}
