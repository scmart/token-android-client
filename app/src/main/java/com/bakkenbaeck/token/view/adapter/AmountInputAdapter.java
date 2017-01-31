package com.bakkenbaeck.token.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.adapter.viewholder.AmountInputViewHolder;
import com.bakkenbaeck.token.view.adapter.viewholder.AmountInputViewHolderBackspace;

public class AmountInputAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int REGULAR_TYPE = 1;
    private static final int BACKSPACE_TYPE = 2;

    private final String[] valueArray = new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "0", "backspace"};
    private OnKeyboardItemClicked listener;

    public void setOnKeyboardItemClickListener(final OnKeyboardItemClicked listener) {
        this.listener = listener;
    }

    public interface OnKeyboardItemClicked {
        void onNumberClicked(final String value);
        void onBackSpaceClicked();
        void onDotClicked();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case BACKSPACE_TYPE: {
                final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_amount_input_backspace, parent, false);
                return new AmountInputViewHolderBackspace(v);
            }
            case REGULAR_TYPE:
            default: {
                final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_amount_input, parent, false);
                return new AmountInputViewHolder(v);
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final String value = valueArray[position];

        switch (holder.getItemViewType()) {
            case REGULAR_TYPE : {
                final AmountInputViewHolder vh = (AmountInputViewHolder) holder;
                vh.setText(String.valueOf(value));
                vh.bind(value, listener);

                if (value.equals("0") || value.equals(".")) {
                    vh.hideDivider();
                }

                break;
            }
            case BACKSPACE_TYPE : {
                final AmountInputViewHolderBackspace vh = (AmountInputViewHolderBackspace) holder;
                vh.bind(listener);
            }
        }
    }


    @Override
    public int getItemViewType(int position) {
        return this.valueArray[position].equals("backspace")
                ? BACKSPACE_TYPE
                : REGULAR_TYPE;
    }

    @Override
    public int getItemCount() {
        return valueArray.length;
    }
}
