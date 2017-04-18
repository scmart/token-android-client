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

package com.tokenbrowser.view.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
import com.tokenbrowser.R;
import com.tokenbrowser.model.local.PendingTransaction;
import com.tokenbrowser.model.sofa.Payment;
import com.tokenbrowser.model.sofa.SofaAdapters;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.adapter.viewholder.TransactionViewHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionViewHolder> {

    private final SofaAdapters adapters;
    private final List<PendingTransaction> transactions;

    public TransactionsAdapter() {
        this.adapters = new SofaAdapters();
        this.transactions = new ArrayList<>();
    }

    public TransactionsAdapter addTransaction(final PendingTransaction transaction) {
        this.transactions.add(0, transaction);
        notifyItemInserted(0);
        return this;
    }

    @Override
    public TransactionViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__transaction, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final TransactionViewHolder holder, final int position) {
        try {
            final PendingTransaction transaction = this.transactions.get(position);
            final String sofaMessage = transaction.getSofaMessage().getPayload();
            final Payment payment = this.adapters.paymentFrom(sofaMessage);
            holder.setPayment(payment);
        } catch (final IOException ex) {
            LogUtil.e(getClass(), ex.toString());
            Crashlytics.logException(ex);
        }
    }

    @Override
    public int getItemCount() {
        return this.transactions.size();
    }

    public void clear() {
        this.transactions.clear();
        notifyDataSetChanged();
    }
}
