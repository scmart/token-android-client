package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.token.model.sofa.Control;
import com.bakkenbaeck.token.view.adapter.ControlAdapter;

public class ControlViewHolder extends RecyclerView.ViewHolder {
    private TextView item;

    public ControlViewHolder(View itemView) {
        super(itemView);
        this.item = (TextView) itemView;
    }

    public void setText(final String text) {
        this.item.setText(text);
    }

    public void bind(final Control control, final ControlAdapter.OnControlClickListener listener) {
        this.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener == null) {
                    return;
                }

                listener.onControlClicked(control);
            }
        });
    }
}
