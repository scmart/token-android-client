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


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tokenbrowser.R;
import com.tokenbrowser.model.sofa.Payment;
import com.tokenbrowser.util.EthUtil;
import com.tokenbrowser.view.BaseApplication;

public class TransactionViewHolder extends RecyclerView.ViewHolder {

    private TextView local_currency_amount;
    private TextView eth_amount;
    private TextView direction;
    private TextView address;
    private ImageView status;

    public TransactionViewHolder(final View view) {
        super(view);
        this.local_currency_amount = (TextView) view.findViewById(R.id.local_currency_amount);
        this.eth_amount = (TextView) view.findViewById(R.id.eth_amount);
        this.direction = (TextView) view.findViewById(R.id.direction);
        this.address = (TextView) view.findViewById(R.id.address);
        this.status = (ImageView) view.findViewById(R.id.status);
    }

    public void setPayment(final Payment payment) {
        renderAmounts(payment);
        setDrawable(payment);
        payment
                .getPaymentDirection()
                .subscribe(direction -> this.handlePaymentDirection(payment, direction));
    }

    private void renderAmounts(final Payment payment) {
        this.local_currency_amount.setText(payment.getLocalPrice());
        final String ethAmount = EthUtil.hexAmountToUserVisibleString(payment.getValue());
        this.eth_amount.setText(ethAmount);
    }

    private void setDrawable(final Payment payment) {
        switch (payment.getStatus()) {
            case "confirmed":
                this.status.setImageResource(R.drawable.ic_done_with_background);
                break;
            case "failed":
                this.status.setImageResource(R.drawable.ic_clear_with_background);
                break;
            default:
                this.status.setImageResource(R.drawable.ic_clock);
        }
    }

    private void handlePaymentDirection(final Payment payment, final @Payment.PaymentDirection int direction) {
        if (direction == Payment.FROM_LOCAL_USER) {
            this.direction.setText(BaseApplication.get().getResources().getString(R.string.payment_to));
            this.address.setText(payment.getToAddress());
        } else {
            this.direction.setText(BaseApplication.get().getResources().getString(R.string.payment_from));
            this.address.setText(payment.getFromAddress());
        }
    }
}