package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.sofa.Control;
import com.bakkenbaeck.token.view.adapter.ControlAdapter;

public class ControlGroupViewHolder extends RecyclerView.ViewHolder {
    private FrameLayout item;
    private TextView label;

    public ControlGroupViewHolder(View itemView) {
        super(itemView);
        this.item = (FrameLayout) itemView;
        this.label = (TextView) itemView.findViewById(R.id.label);
    }

    public void setText(final String text) {
        this.label.setText(text);
    }

    public void bind(final Control control, final ControlAdapter.OnControlClickListener listener) {
        this.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener == null) {
                    return;
                }

                listener.onGroupedControlItemClicked(control);
            }
        });
    }
}
