package com.bakkenbaeck.token.view.adapter.viewholder;

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.sofa.Control;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.adapter.ControlAdapter;

public class ControlGroupViewHolder extends RecyclerView.ViewHolder {
    private FrameLayout item;
    private TextView label;
    private ImageView arrow;
    private boolean isFocused;

    public ControlGroupViewHolder(View itemView) {
        super(itemView);
        this.item = (FrameLayout) itemView;
        this.label = (TextView) itemView.findViewById(R.id.label);
        this.arrow = (ImageView) itemView.findViewById(R.id.arrow);
    }

    public void setText(final String text) {
        this.label.setText(text);
    }

    public void bind(final int position, final ControlAdapter adapter) {
        this.item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateSelectedState(position, adapter);
            }
        });
    }

    private void updateSelectedState(final int position, final ControlAdapter adapter) {
        final ControlAdapter.OnControlClickListener listener = adapter.getControlClickListener();

        if (listener == null) {
            return;
        }

        final boolean isFocusedAndSelected = isFocused && position == adapter.getSelectedPos();
        final boolean isUnfocusedAndSelected = !isFocused && position == adapter.getSelectedPos();
        final Control control = adapter.getControl(position);

        if (isFocusedAndSelected) {
            unSelect(listener);
        } else if(isUnfocusedAndSelected) {
            select(control, listener);
        } else {
            select(control, listener);
            adapter.updateView(position);
        }
    }

    private void select(final Control control, final ControlAdapter.OnControlClickListener listener) {
        final int selectedColor = ContextCompat.getColor(BaseApplication.get(), R.color.control_selected_text_color);
        final Drawable selectedDrawable = ContextCompat.getDrawable(BaseApplication.get(), R.drawable.ic_arrow_up_selected);
        this.label.setTextColor(selectedColor);
        this.arrow.setImageDrawable(selectedDrawable);
        isFocused = true;
        listener.onGroupedControlItemClicked(control);
    }

    public void unSelect(final ControlAdapter.OnControlClickListener listener) {
        unselectView();
        listener.onGroupedControlItemClicked(null);
    }

    public void unselectView() {
        final int selectedColor = ContextCompat.getColor(BaseApplication.get(), R.color.control_text_color);
        final Drawable unselectedDrawable = ContextCompat.getDrawable(BaseApplication.get(), R.drawable.ic_arrow_up);
        this.label.setTextColor(selectedColor);
        this.arrow.setImageDrawable(unselectedDrawable);
        isFocused = false;
    }
}
