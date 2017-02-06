package com.bakkenbaeck.token.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.local.ChatMessage;
import com.bakkenbaeck.token.model.sofa.Message;
import com.bakkenbaeck.token.model.sofa.Payment;
import com.bakkenbaeck.token.model.sofa.PaymentRequest;
import com.bakkenbaeck.token.model.sofa.SofaAdapters;
import com.bakkenbaeck.token.model.sofa.SofaType;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.adapter.listeners.OnItemClickListener;
import com.bakkenbaeck.token.view.adapter.viewholder.PaymentRequestViewHolder;
import com.bakkenbaeck.token.view.adapter.viewholder.PaymentViewHolder;
import com.bakkenbaeck.token.view.adapter.viewholder.TextViewHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public final class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ChatMessage> chatMessages;
    private final SofaAdapters adapters;
    private OnItemClickListener<ChatMessage> onPaymentRequestApproveListener;
    private OnItemClickListener<ChatMessage> onPaymentRequestRejectListener;

    public MessageAdapter() {
        this.chatMessages = new ArrayList<>();
        this.adapters = new SofaAdapters();
    }

    public final MessageAdapter addOnPaymentRequestApproveListener(final OnItemClickListener<ChatMessage> listener) {
        this.onPaymentRequestApproveListener = listener;
        return this;
    }

    public final MessageAdapter addOnPaymentRequestRejectListener(final OnItemClickListener<ChatMessage> listener) {
        this.onPaymentRequestRejectListener = listener;
        return this;
    }

    public final void addMessages(final Collection<ChatMessage> chatMessages) {
        this.chatMessages.clear();

        for (ChatMessage chatMessage : chatMessages) {
            if (shouldShowChatMessage(chatMessage)) {
                this.chatMessages.add(chatMessage);
            }
        }

        notifyDataSetChanged();
    }

    private boolean shouldShowChatMessage(final ChatMessage chatMessage) {
        return chatMessage.getType() != SofaType.UNKNOWN
                && chatMessage.getType() != SofaType.INIT
                && chatMessage.getType() != SofaType.INIT_REQUEST
                && chatMessage.getType() != SofaType.COMMAND_REQUEST;
    }

    public final void addMessage(final ChatMessage chatMessage) {
        this.chatMessages.add(chatMessage);
        notifyItemInserted(this.chatMessages.size() - 1);
    }

    public final void updateMessage(final ChatMessage chatMessage) {
        final int position = this.chatMessages.indexOf(chatMessage);
        if (position == -1) {
            addMessage(chatMessage);
            return;
        }

        this.chatMessages.set(position, chatMessage);
        notifyItemChanged(position);
    }

    @Override
    public int getItemViewType(final int position) {
        final ChatMessage chatMessage = this.chatMessages.get(position);
        return chatMessage.getType();
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(
            final ViewGroup parent,
            final int viewType) {

        switch (viewType) {

            case SofaType.PAYMENT_REQUEST: {
                final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__payment_request, parent, false);
                return new PaymentRequestViewHolder(v);
            }

            case SofaType.PAYMENT: {
                final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__payment, parent, false);
                return new PaymentViewHolder(v);
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
    public final void onBindViewHolder(
            final RecyclerView.ViewHolder holder,
            final int position) {

        final ChatMessage chatMessage = this.chatMessages.get(position);
        final String payload = chatMessage.getPayload();

        if (payload == null) {
            return;
        }

        try {
            renderChatMessageIntoViewHolder(holder, chatMessage, payload);
        } catch (final IOException ex) {
            LogUtil.error(getClass(), "Unable to render view holder: " + ex);
        }
    }

    private void renderChatMessageIntoViewHolder(
            final RecyclerView.ViewHolder holder,
            final ChatMessage chatMessage,
            final String payload) throws IOException {

        switch (holder.getItemViewType()) {
            case SofaType.PLAIN_TEXT: {
                final TextViewHolder vh = (TextViewHolder) holder;
                final Message message = this.adapters.messageFrom(payload);
                vh.setText(message.getBody())
                  .setSentByLocal(chatMessage.isSentByLocal())
                  .setSendState(chatMessage.getSendState())
                  .draw();
                break;
            }

            case SofaType.PAYMENT: {
                final PaymentViewHolder vh = (PaymentViewHolder) holder;
                final Payment payment = this.adapters.paymentFrom(payload);
                vh.setPayment(payment)
                  .setSendState(chatMessage.getSendState())
                  .setSentByLocal(chatMessage.isSentByLocal())
                  .draw();
                break;
            }

            case SofaType.PAYMENT_REQUEST: {
                final PaymentRequestViewHolder vh = (PaymentRequestViewHolder) holder;
                final PaymentRequest request = this.adapters.txRequestFrom(payload);
                vh.setPaymentRequest(request)
                  .setSentByLocal(chatMessage.isSentByLocal())
                  .setOnApproveListener(this.handleOnPaymentRequestApproved)
                  .setOnRejectListener(this.handleOnPaymentRequestRejected)
                  .draw();
                break;
            }
        }
    }

    @Override
    public final int getItemCount() {
        return this.chatMessages.size();
    }

    private final OnItemClickListener<Integer> handleOnPaymentRequestApproved = new OnItemClickListener<Integer>() {
        @Override
        public void onItemClick(final Integer position) {
            if (onPaymentRequestApproveListener == null) return;

            final ChatMessage chatMessage = chatMessages.get(position);
            onPaymentRequestApproveListener.onItemClick(chatMessage);
        }
    };

    private final OnItemClickListener<Integer> handleOnPaymentRequestRejected = new OnItemClickListener<Integer>() {
        @Override
        public void onItemClick(final Integer position) {
            if (onPaymentRequestRejectListener == null) return;

            final ChatMessage chatMessage = chatMessages.get(position);
            onPaymentRequestRejectListener.onItemClick(chatMessage);
        }
    };
}