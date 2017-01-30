package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.adapter.AmountInputAdapter;

public class AmountInputViewHolder extends RecyclerView.ViewHolder {
    private TextView value;
    private View divider;

    public AmountInputViewHolder(View itemView) {
        super(itemView);

        this.value = (TextView) itemView.findViewById(R.id.value);
        this.divider = itemView.findViewById(R.id.divider);
    }

    public void setText(final String value) {
        this.value.setText(value);
    }

    public void bind(final String value, final AmountInputAdapter.OnKeyboardItemClicked listener) {
        this.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener == null) {
                    return;
                }

                if (value.equals(".")) {
                    listener.onDotClicked();
                } else {
                    listener.onNumberClicked(value);
                }
            }
        });
    }

    public void hideDivider() {
        this.divider.setVisibility(View.GONE);
    }
}
