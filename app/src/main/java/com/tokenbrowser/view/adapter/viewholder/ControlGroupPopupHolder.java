package com.tokenbrowser.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tokenbrowser.R;
import com.tokenbrowser.model.sofa.Control;
import com.tokenbrowser.view.adapter.ControlGroupAdapter;

public class ControlGroupPopupHolder extends RecyclerView.ViewHolder {
    private TextView label;
    private ImageView arrow;

    public ControlGroupPopupHolder(View itemView) {
        super(itemView);
        this.label = (TextView) itemView.findViewById(R.id.label);
        this.arrow = (ImageView) itemView.findViewById(R.id.arrow);
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

    public void showArrow() {
        this.arrow.setVisibility(View.VISIBLE);
    }

    public void hideArrow() {
        this.arrow.setVisibility(View.GONE);
    }
}