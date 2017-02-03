package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.util.TypeConverter;
import com.bakkenbaeck.token.model.local.SendState;
import com.bakkenbaeck.token.model.sofa.Payment;
import com.bakkenbaeck.token.util.EthUtil;
import com.bakkenbaeck.token.view.BaseApplication;

import java.math.BigInteger;

public final class PaymentViewHolder extends RecyclerView.ViewHolder {

    private View localView;
    private View remoteView;
    private TextView localMessageText;
    private TextView remoteMessageText;
    private TextView localRequestedAmount;
    private TextView remoteRequestedAmount;
    private TextView localSecondaryAmount;
    private TextView remoteSecondaryAmount;
    private TextView sentFailedMessage;


    private Payment payment;
    private boolean sentByLocal;
    private @SendState.State int sendState;

    public PaymentViewHolder(final View v) {
        super(v);
        this.localView = v.findViewById(R.id.local);
        this.remoteView = v.findViewById(R.id.remote);
        this.localMessageText = (TextView) v.findViewById(R.id.local_message);
        this.remoteMessageText = (TextView) v.findViewById(R.id.remote_message);
        this.localRequestedAmount = (TextView) v.findViewById(R.id.local_requested_amount);
        this.remoteRequestedAmount = (TextView) v.findViewById(R.id.remote_requested_amount);
        this.localSecondaryAmount = (TextView) v.findViewById(R.id.local_eth_amount);
        this.remoteSecondaryAmount = (TextView) v.findViewById(R.id.remote_eth_amount);
        this.sentFailedMessage = (TextView) v.findViewById(R.id.sent_fail_message);
    }

    public PaymentViewHolder setPayment(final Payment payment) {
        this.payment = payment;
        return this;
    }

    public PaymentViewHolder setSentByLocal(final boolean sentByLocal) {
        this.sentByLocal = sentByLocal;
        return this;
    }

    public PaymentViewHolder setSendState(final @SendState.State int sendState) {
        this.sendState = sendState;
        return this;
    }

    public void draw() {
        final String ethAmount = getFormattedEthAmount();

        if (this.sentByLocal) {
            this.localView.setVisibility(View.VISIBLE);
            this.remoteView.setVisibility(View.GONE);
            this.localRequestedAmount.setText(this.payment.getLocalPrice());


            this.localSecondaryAmount.setText(ethAmount);
            renderPaymentStatusMessage();
        } else {
            this.remoteView.setVisibility(View.VISIBLE);
            this.localView.setVisibility(View.GONE);
            this.remoteRequestedAmount.setText(this.payment.getLocalPrice());
            this.remoteSecondaryAmount.setText(ethAmount);
        }

        this.payment = null;
    }

    private void renderPaymentStatusMessage() {

        if (this.payment.getStatus() != null && this.payment.getStatus().equals("confirmed")) {
            this.sentFailedMessage.setVisibility(View.VISIBLE);
            this.sentFailedMessage.setText(R.string.error__transaction_succeeded);
            return;
        }

        switch (this.sendState) {
            case SendState.STATE_FAILED:
                this.sentFailedMessage.setVisibility(View.VISIBLE);
                this.sentFailedMessage.setText(R.string.error__transaction_failed);
                break;
            case SendState.STATE_SENDING:
            case SendState.STATE_SENT:
                this.sentFailedMessage.setVisibility(View.VISIBLE);
                this.sentFailedMessage.setText(R.string.error__transaction_pending);
                break;
            case SendState.STATE_RECEIVED:
            case SendState.STATE_LOCAL_ONLY:
            default:
                this.sentFailedMessage.setVisibility(View.GONE);
                break;
        }
    }

    private String getFormattedEthAmount() {
        final BigInteger weiAmount = TypeConverter.StringHexToBigInteger(this.payment.getValue());
        final String ethAmount = EthUtil.weiToEthString(weiAmount);
        return String.format(
                BaseApplication.get().getResources().getString(R.string.eth_amount),
                ethAmount);
    }

}
