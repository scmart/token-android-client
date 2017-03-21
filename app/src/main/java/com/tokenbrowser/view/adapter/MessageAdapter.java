package com.tokenbrowser.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.model.local.SofaMessage;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.sofa.Message;
import com.tokenbrowser.model.sofa.Payment;
import com.tokenbrowser.model.sofa.PaymentRequest;
import com.tokenbrowser.model.sofa.SofaAdapters;
import com.tokenbrowser.model.sofa.SofaType;
import com.tokenbrowser.token.R;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;
import com.tokenbrowser.view.adapter.viewholder.PaymentRequestViewHolder;
import com.tokenbrowser.view.adapter.viewholder.PaymentViewHolder;
import com.tokenbrowser.view.adapter.viewholder.TextViewHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public final class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<SofaMessage> sofaMessages;
    private final SofaAdapters adapters;
    private OnItemClickListener<SofaMessage> onPaymentRequestApproveListener;
    private OnItemClickListener<SofaMessage> onPaymentRequestRejectListener;
    private OnItemClickListener<String> onUsernameClickListener;
    private OnItemClickListener<String> onImageClickListener;

    public MessageAdapter() {
        this.sofaMessages = new ArrayList<>();
        this.adapters = new SofaAdapters();
    }

    public final MessageAdapter addOnPaymentRequestApproveListener(final OnItemClickListener<SofaMessage> listener) {
        this.onPaymentRequestApproveListener = listener;
        return this;
    }

    public final MessageAdapter addOnPaymentRequestRejectListener(final OnItemClickListener<SofaMessage> listener) {
        this.onPaymentRequestRejectListener = listener;
        return this;
    }

    public final MessageAdapter addOnUsernameClickListener(final OnItemClickListener<String> listener) {
        this.onUsernameClickListener = listener;
        return this;
    }

    public final MessageAdapter addOnImageClickListener(final OnItemClickListener<String> listener) {
        this.onImageClickListener = listener;
        return this;
    }

    public final void addMessages(final Collection<SofaMessage> sofaMessages) {
        this.sofaMessages.clear();

        for (SofaMessage sofaMessage : sofaMessages) {
            if (shouldShowChatMessage(sofaMessage)) {
                this.sofaMessages.add(sofaMessage);
            }
        }

        notifyDataSetChanged();
    }

    private boolean shouldShowChatMessage(final SofaMessage sofaMessage) {
        return sofaMessage.getType() != SofaType.UNKNOWN
                && sofaMessage.getType() != SofaType.INIT
                && sofaMessage.getType() != SofaType.INIT_REQUEST;
    }

    public final void addMessage(final SofaMessage sofaMessage) {
        this.sofaMessages.add(sofaMessage);
        notifyItemInserted(this.sofaMessages.size() - 1);
    }

    public final void updateMessage(final SofaMessage sofaMessage) {
        final int position = this.sofaMessages.indexOf(sofaMessage);
        if (position == -1) {
            addMessage(sofaMessage);
            return;
        }

        this.sofaMessages.set(position, sofaMessage);
        notifyItemChanged(position);
    }

    @Override
    public int getItemViewType(final int position) {
        final SofaMessage sofaMessage = this.sofaMessages.get(position);
        return sofaMessage.getType();
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

        final SofaMessage sofaMessage = this.sofaMessages.get(position);
        final String payload = sofaMessage.getPayload();

        if (payload == null) {
            return;
        }

        try {
            renderChatMessageIntoViewHolder(holder, sofaMessage, payload);
        } catch (final IOException ex) {
            LogUtil.error(getClass(), "Unable to render view holder: " + ex);
        }
    }

    private void renderChatMessageIntoViewHolder(
            final RecyclerView.ViewHolder holder,
            final SofaMessage sofaMessage,
            final String payload) throws IOException {

        final User localUser = getCurrentLocalUser();

        switch (holder.getItemViewType()) {
            case SofaType.COMMAND_REQUEST:
            case SofaType.PLAIN_TEXT: {
                final TextViewHolder vh = (TextViewHolder) holder;
                final Message message = this.adapters.messageFrom(payload)
                        .setAttachmentFilename(sofaMessage.getAttachmentFilePath());
                vh.setText(message.getBody())
                        .setSentByLocal(sofaMessage.isSentBy(localUser))
                        .setAvatarUri(sofaMessage.getSender() != null ? sofaMessage.getSender().getAvatar() : null)
                        .setSendState(sofaMessage.getSendState())
                        .setAttachmentFilePath(sofaMessage.getAttachmentFilePath())
                        .setClickableImage(this.onImageClickListener, sofaMessage.getAttachmentFilePath())
                        .draw()
                        .setClickableUsernames(this.onUsernameClickListener);
                break;
            }

            case SofaType.PAYMENT: {
                final PaymentViewHolder vh = (PaymentViewHolder) holder;
                final Payment payment = this.adapters.paymentFrom(payload);
                vh.setPayment(payment)
                  .setSendState(sofaMessage.getSendState())
                  .setSentByLocal(sofaMessage.isSentBy(localUser))
                  .draw();
                break;
            }

            case SofaType.PAYMENT_REQUEST: {
                final PaymentRequestViewHolder vh = (PaymentRequestViewHolder) holder;
                final PaymentRequest request = this.adapters.txRequestFrom(payload);
                vh.setPaymentRequest(request)
                  .setSentByLocal(sofaMessage.isSentBy(localUser))
                  .setOnApproveListener(this.handleOnPaymentRequestApproved)
                  .setOnRejectListener(this.handleOnPaymentRequestRejected)
                  .draw();
                break;
            }
        }
    }

    @Override
    public final int getItemCount() {
        return this.sofaMessages.size();
    }

    private final OnItemClickListener<Integer> handleOnPaymentRequestApproved = new OnItemClickListener<Integer>() {
        @Override
        public void onItemClick(final Integer position) {
            if (onPaymentRequestApproveListener == null) return;

            final SofaMessage sofaMessage = sofaMessages.get(position);
            onPaymentRequestApproveListener.onItemClick(sofaMessage);
        }
    };

    private final OnItemClickListener<Integer> handleOnPaymentRequestRejected = new OnItemClickListener<Integer>() {
        @Override
        public void onItemClick(final Integer position) {
            if (onPaymentRequestRejectListener == null) return;

            final SofaMessage sofaMessage = sofaMessages.get(position);
            onPaymentRequestRejectListener.onItemClick(sofaMessage);
        }
    };

    public void clear() {
        this.sofaMessages.clear();
        notifyDataSetChanged();
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