package com.tokenbrowser.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.tokenbrowser.R;
import com.tokenbrowser.model.sofa.Control;
import com.tokenbrowser.view.adapter.ControlAdapter;

public class ControlViewHolder extends RecyclerView.ViewHolder {
    private TextView item;

    public ControlViewHolder(View itemView) {
        super(itemView);
        this.item = (TextView) itemView.findViewById(R.id.control_label);
    }

    public void setText(final String text) {
        this.item.setText(text);
    }

    public void bind(final Control control, final ControlAdapter.OnControlClickListener listener) {
        this.itemView.setOnClickListener(new View.OnClickListener() {
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
