package com.tokenbrowser.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.tokenbrowser.token.R;
import com.tokenbrowser.model.sofa.PaymentRequest;
import com.tokenbrowser.util.EthUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;

public final class PaymentRequestViewHolder extends RecyclerView.ViewHolder {

    private View localView;
    private View remoteView;
    private TextView localRequestedAmount;
    private TextView remoteRequestedAmount;
    private TextView localSecondaryAmount;
    private TextView remoteSecondaryAmount;
    private ImageView remoteStatusIcon;
    private TextView remoteStatus;
    private View buttonsContainer;
    private Button approveButton;
    private Button rejectButton;

    private OnItemClickListener<Integer> onApproveListener;
    private OnItemClickListener<Integer> onRejectListener;

    private PaymentRequest request;
    private boolean sentByLocal;

    public PaymentRequestViewHolder(final View v) {
        super(v);
        this.localView = v.findViewById(R.id.local);
        this.remoteView = v.findViewById(R.id.remote);
        this.localRequestedAmount = (TextView) v.findViewById(R.id.local_requested_amount);
        this.remoteRequestedAmount = (TextView) v.findViewById(R.id.remote_requested_amount);
        this.localSecondaryAmount = (TextView) v.findViewById(R.id.local_eth_amount);
        this.remoteSecondaryAmount = (TextView) v.findViewById(R.id.remote_eth_amount);
        this.remoteStatusIcon = (ImageView) v.findViewById(R.id.request_status_icon);
        this.remoteStatus = (TextView) v.findViewById(R.id.request_status);
        this.buttonsContainer = v.findViewById(R.id.buttons_container);
        this.approveButton = (Button) v.findViewById(R.id.approve_button);
        this.rejectButton = (Button) v.findViewById(R.id.reject_button);
    }

    public PaymentRequestViewHolder setPaymentRequest(final PaymentRequest request) {
        this.request = request;
        return this;
    }

    public PaymentRequestViewHolder setSentByLocal(final boolean sentByLocal) {
        this.sentByLocal = sentByLocal;
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
        if (this.sentByLocal) {
            this.localView.setVisibility(View.VISIBLE);
            this.remoteView.setVisibility(View.GONE);
            this.localRequestedAmount.setText(this.request.getLocalPrice());
            final String ethAmount = String.format(
                    BaseApplication.get().getResources().getString(R.string.eth_amount),
                    EthUtil.valueToEthString(this.request.getValue()));
            this.localSecondaryAmount.setText(ethAmount);
        } else {
            this.remoteView.setVisibility(View.VISIBLE);
            this.localView.setVisibility(View.GONE);
            this.remoteRequestedAmount.setText(this.request.getLocalPrice());
            final String ethAmount = String.format(
                    BaseApplication.get().getResources().getString(R.string.eth_amount),
                    EthUtil.valueToEthString(this.request.getValue()));
            this.remoteSecondaryAmount.setText(ethAmount);
            setStatus(this.request.getState());
        }
    }

    private void setStatus(final @PaymentRequest.State int state) {
        switch (state) {
            case PaymentRequest.ACCEPTED:
                this.buttonsContainer.setVisibility(View.GONE);
                this.remoteStatus.setVisibility(View.VISIBLE);
                this.remoteStatus.setText(R.string.payment_request__accepted);
                this.remoteStatusIcon.setVisibility(View.VISIBLE);
                this.remoteStatusIcon.setImageResource(R.drawable.ic_done_with_background);
                break;
            case PaymentRequest.REJECTED:
                this.buttonsContainer.setVisibility(View.GONE);
                this.remoteStatus.setVisibility(View.VISIBLE);
                this.remoteStatus.setText(R.string.payment_request__rejected);
                this.remoteStatusIcon.setVisibility(View.VISIBLE);
                this.remoteStatusIcon.setImageResource(R.drawable.ic_clear_with_background);
                break;
            case PaymentRequest.PENDING:
            default:
                this.buttonsContainer.setVisibility(View.VISIBLE);
                this.remoteStatus.setVisibility(View.GONE);
                this.remoteStatusIcon.setVisibility(View.GONE);
                this.rejectButton.setOnClickListener(this.handleOnRejectPressed);
                this.approveButton.setOnClickListener(this.handleOnApprovePressed);
                break;
        }
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
