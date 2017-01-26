package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.sofa.Control;
import com.bakkenbaeck.token.view.adapter.ControlGroupAdapter;

public class ControlGroupPopupHolder extends RecyclerView.ViewHolder {
    private TextView label;

    public ControlGroupPopupHolder(View itemView) {
        super(itemView);
        this.label = (TextView) itemView.findViewById(R.id.label);
    }

    public void setText(final String text) {
        this.label.setText(text);
    }

    public void bind(final Control control, final ControlGroupAdapter.OnItemClickListener listener) {
        this.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener == null) {
                    return;
                }

                listener.onItemClicked(control);
            }
        });
    }
}