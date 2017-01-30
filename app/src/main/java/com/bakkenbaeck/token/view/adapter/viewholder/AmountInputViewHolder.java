package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.token.R;

public class AmountInputViewHolder extends RecyclerView.ViewHolder {
    private TextView value;

    public AmountInputViewHolder(View itemView) {
        super(itemView);

        this.value = (TextView) itemView.findViewById(R.id.value);
    }

    public void setText(final String value) {
        this.value.setText(value);
    }
}
