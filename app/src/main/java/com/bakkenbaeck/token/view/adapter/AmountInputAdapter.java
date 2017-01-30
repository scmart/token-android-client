package com.bakkenbaeck.token.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.adapter.viewholder.AmountInputViewHolder;

public class AmountInputAdapter extends RecyclerView.Adapter<AmountInputViewHolder> {

    private static final Integer[] valueArray = new Integer[] {1, 2, 3, 4, 5, 6, 7, 8, 9};

    @Override
    public AmountInputViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_amount_input, parent, false);
        return new AmountInputViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AmountInputViewHolder holder, int position) {
        final int value = valueArray[position];

        holder.setText(String.valueOf(value));
    }

    @Override
    public int getItemCount() {
        return valueArray.length;
    }
}
