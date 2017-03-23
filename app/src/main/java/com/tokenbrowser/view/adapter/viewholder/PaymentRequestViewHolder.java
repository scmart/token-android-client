package com.tokenbrowser.view.adapter.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tokenbrowser.model.sofa.PaymentRequest;
import com.tokenbrowser.token.R;
import com.tokenbrowser.util.EthUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;

public final class PaymentRequestViewHolder extends RecyclerView.ViewHolder {

    private @NonNull TextView requestedAmount;
    private @NonNull TextView ethereumAmount;
    private @Nullable ImageView statusIcon;
    private @Nullable TextView statusMessage;
    private @Nullable View acceptButtonContainer;
    private @Nullable View declineButtonContainer;
    private @Nullable Button acceptButton;
    private @Nullable Button declineButton;
    private @Nullable ImageView avatar;

    private OnItemClickListener<Integer> onApproveListener;
    private OnItemClickListener<Integer> onRejectListener;

    private PaymentRequest request;
    private String avatarUri;

    public PaymentRequestViewHolder(final View v) {
        super(v);
        this.requestedAmount = (TextView) v.findViewById(R.id.requested_amount);
        this.ethereumAmount = (TextView) v.findViewById(R.id.eth_amount);
        this.statusIcon = (ImageView) v.findViewById(R.id.status_icon);
        this.statusMessage = (TextView) v.findViewById(R.id.status_message);
        this.acceptButtonContainer = v.findViewById(R.id.container_accept_button);
        this.declineButtonContainer = v.findViewById(R.id.container_decline_button);
        this.acceptButton = (Button) v.findViewById(R.id.approve_button);
        this.declineButton = (Button) v.findViewById(R.id.reject_button);
        this.avatar = (ImageView) v.findViewById(R.id.avatar);
    }

    public PaymentRequestViewHolder setPaymentRequest(final PaymentRequest request) {
        this.request = request;
        return this;
    }

    public PaymentRequestViewHolder setAvatarUri(final String uri) {
        this.avatarUri = uri;
        return this;
    }

    public PaymentRequestViewHolder setOnApproveListener(final OnItemClickListener<Integer> onApproveListener) {
        this.onApproveListener = onApproveListener;
        return this;
    }

    public PaymentRequestViewHolder setOnRejectListener(final OnItemClickListener<Integer> onRejectListener) {
        this.onRejectListener = onRejectListener;
        return this;
    }

    public void draw() {
        renderAmounts();
        renderStatus();
        renderAvatar();
    }

    private void renderAmounts() {
        this.requestedAmount.setText(this.request.getLocalPrice());
        final String ethAmount = String.format(
                BaseApplication.get().getResources().getString(R.string.eth_amount),
                EthUtil.hexAmountToUserVisibleString(this.request.getValue()));
        this.ethereumAmount.setText(ethAmount);
    }

    private void renderStatus() {
        if (this.statusMessage == null) {
            return;
        }

        this.acceptButtonContainer.setVisibility(View.GONE);
        this.declineButtonContainer.setVisibility(View.GONE);
        this.statusMessage.setVisibility(View.VISIBLE);
        this.statusIcon.setVisibility(View.VISIBLE);

        final @PaymentRequest.State int state = this.request.getState();
        switch (state) {
            case PaymentRequest.ACCEPTED:
                this.statusMessage.setText(R.string.payment_request__accepted);
                this.statusIcon.setImageResource(R.drawable.ic_done_with_background);
                break;
            case PaymentRequest.REJECTED:
                this.statusMessage.setText(R.string.payment_request__rejected);
                this.statusIcon.setImageResource(R.drawable.ic_clear_with_background);
                break;
            case PaymentRequest.PENDING:
            default:
                this.acceptButtonContainer.setVisibility(View.VISIBLE);
                this.declineButtonContainer.setVisibility(View.VISIBLE);
                this.statusMessage.setVisibility(View.GONE);
                this.statusIcon.setVisibility(View.GONE);
                this.declineButton.setOnClickListener(this.handleOnRejectPressed);
                this.acceptButton.setOnClickListener(this.handleOnApprovePressed);
                break;
        }
    }

    private void renderAvatar() {
        if (this.avatar == null) {
            return;
        }

        Glide
                .with(this.avatar.getContext())
                .load(this.avatarUri)
                .into(this.avatar);
    }

    private final View.OnClickListener handleOnApprovePressed = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (onApproveListener == null) return;
            onApproveListener.onItemClick(getAdapterPosition());
        }
    };

    private final View.OnClickListener handleOnRejectPressed = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (onRejectListener == null) return;
            onRejectListener.onItemClick(getAdapterPosition());
        }
    };
}
