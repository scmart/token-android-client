package com.tokenbrowser.view.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.R;
import com.tokenbrowser.model.local.PendingTransaction;
import com.tokenbrowser.view.adapter.viewholder.TransactionViewHolder;

import java.util.ArrayList;
import java.util.List;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionViewHolder> {

    private List<PendingTransaction> transactions;

    public TransactionsAdapter() {
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
        final PendingTransaction transaction = this.transactions.get(position);
        holder.setTransaction(transaction);
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
