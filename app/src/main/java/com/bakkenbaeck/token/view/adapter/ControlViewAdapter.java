package com.bakkenbaeck.token.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.sofa.Control;
import com.bakkenbaeck.token.view.adapter.viewholder.ControlViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ControlViewAdapter extends RecyclerView.Adapter<ControlViewHolder> {

    private List<Control> controls;

    public ControlViewAdapter(final List<Control> controls) {
        this.controls = new ArrayList<>(controls);
    }

    @Override
    public ControlViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__control, parent, false);
        return new ControlViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ControlViewHolder holder, int position) {
        final Control control = this.controls.get(position);
        holder.setText(control.getLabel());
    }

    @Override
    public int getItemCount() {
        return this.controls.size();
    }
}
