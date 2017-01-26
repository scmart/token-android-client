package com.bakkenbaeck.token.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.sofa.Control;
import com.bakkenbaeck.token.model.sofa.SofaType;
import com.bakkenbaeck.token.view.adapter.viewholder.ControlGroupPopupHolder;

import java.util.ArrayList;
import java.util.List;

public class ControlGroupAdapter extends RecyclerView.Adapter<ControlGroupPopupHolder> {

    public interface OnItemClickListener {
        void onItemClicked(final Control control);
    }

    private List<Control> controls;
    private OnItemClickListener listener;

    public void setOnItemClickListener(final OnItemClickListener listener) {
        this.listener = listener;
    }

    public ControlGroupAdapter(final List<Control> controls) {
        this.controls = new ArrayList<>(controls);
    }

    public void setControls(final List<Control> controls) {
        this.controls.clear();
        this.controls.addAll(controls);
        this.notifyDataSetChanged();
    }

    @Override
    public ControlGroupPopupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__grouped_control_popup, parent, false);
        return new ControlGroupPopupHolder(v);
    }

    @Override
    public void onBindViewHolder(ControlGroupPopupHolder holder, int position) {
        final Control control = this.controls.get(position);
        holder.setText(control.getLabel());
        holder.bind(control, listener);

        if (control.getAction().toLowerCase().contains(SofaType.WEB_VIEW)) {
            holder.showArrow();
        } else {
            holder.hideArrow();
        }
    }

    @Override
    public int getItemCount() {
        return this.controls.size();
    }
}