/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.view.adapter.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tokenbrowser.R;
import com.tokenbrowser.crypto.util.TypeConverter;
import com.tokenbrowser.model.local.SendState;
import com.tokenbrowser.model.sofa.Payment;
import com.tokenbrowser.model.sofa.SofaType;
import com.tokenbrowser.util.EthUtil;
import com.tokenbrowser.util.ImageUtil;
import com.tokenbrowser.view.BaseApplication;

import java.math.BigInteger;

public final class PaymentViewHolder extends RecyclerView.ViewHolder {

    private @NonNull TextView requestedAmount;
    private @NonNull TextView ethereumAmount;
    private @NonNull TextView statusMessage;
    private @NonNull ImageView statusIcon;
    private @Nullable ImageView avatar;

    private Payment payment;
    private @SendState.State int sendState;
    private String avatarUri;

    public PaymentViewHolder(final View v) {
        super(v);
        this.requestedAmount = (TextView) v.findViewById(R.id.requested_amount);
        this.ethereumAmount = (TextView) v.findViewById(R.id.eth_amount);
        this.statusIcon = (ImageView) v.findViewById(R.id.status_icon);
        this.statusMessage = (TextView) v.findViewById(R.id.status_message);
        this.avatar = (ImageView) v.findViewById(R.id.avatar);
    }

    public PaymentViewHolder setPayment(final Payment payment) {
        this.payment = payment;
        return this;
    }

    public PaymentViewHolder setAvatarUri(final String uri) {
        this.avatarUri = uri;
        return this;
    }

    public PaymentViewHolder setSendState(final @SendState.State int sendState) {
        this.sendState = sendState;
        return this;
    }

    public void draw() {
        renderAmounts();
        renderAvatar();
        renderPaymentStatus();

        this.payment = null;
    }

    private void renderAmounts() {
        final String ethAmount = getFormattedEthAmount();
        this.requestedAmount.setText(this.payment.getLocalPrice());
        this.ethereumAmount.setText(ethAmount);
    }

    private void renderAvatar() {
        if (this.avatar == null) {
            return;
        }

        ImageUtil.loadFromNetwork(this.avatarUri, this.avatar);
    }

    private void renderPaymentStatus() {
        this.statusMessage.setVisibility(View.VISIBLE);
        this.statusIcon.setVisibility(View.VISIBLE);

        if (this.payment.getStatus() != null && this.payment.getStatus().equals(SofaType.CONFIRMED)) {
            this.statusMessage.setText(R.string.error__transaction_succeeded);
            this.statusIcon.setImageResource(R.drawable.ic_done_with_background);
            return;
        }

        switch (this.sendState) {
            case SendState.STATE_FAILED:
                this.statusMessage.setText(R.string.error__transaction_failed);
                this.statusIcon.setImageResource(R.drawable.ic_clear_with_background);
                break;
            case SendState.STATE_SENDING:
            case SendState.STATE_SENT:
                this.statusMessage.setText(R.string.error__transaction_pending);
                this.statusIcon.setImageResource(R.drawable.ic_clock);
                break;
            case SendState.STATE_PENDING:
            case SendState.STATE_RECEIVED:
            case SendState.STATE_LOCAL_ONLY:
            default:
                this.statusMessage.setVisibility(View.GONE);
                this.statusIcon.setVisibility(View.GONE);
                break;
        }
    }

    private String getFormattedEthAmount() {
        final BigInteger weiAmount = TypeConverter.StringHexToBigInteger(this.payment.getValue());
        final String ethAmount = EthUtil.weiAmountToUserVisibleString(weiAmount);
        return String.format(
                BaseApplication.get().getResources().getString(R.string.eth_amount),
                ethAmount);
    }
}
