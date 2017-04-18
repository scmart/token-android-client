/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.R;
import com.tokenbrowser.model.sofa.Control;
import com.tokenbrowser.view.adapter.viewholder.ControlGroupViewHolder;
import com.tokenbrowser.view.adapter.viewholder.ControlViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ControlAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int CONTROL_VIEW = 1;
    private final static int CONTROL_VIEW_GROUPED = 2;

    public interface OnControlClickListener {
        void onGroupedControlItemClicked(final Control control);
        void onControlClicked(final Control control);
    }

    private final List<Control> controls;
    private OnControlClickListener listener;
    private int selectedPosition = -1;

    public ControlAdapter(final List<Control> controls) {
        this.controls = new ArrayList<>(controls);
    }

    public void setControlClickedListener(final OnControlClickListener listener) {
        this.listener = listener;
    }

    public void setControls(final List<Control> controls) {
        this.controls.clear();
        this.controls.addAll(controls);
        this.notifyDataSetChanged();
    }

    public void updateView(final int position) {
        this.notifyItemChanged(this.selectedPosition);
        this.selectedPosition = position;
    }

    public int getSelectedPos() {
        return this.selectedPosition;
    }

    public OnControlClickListener getControlClickListener() {
        return this.listener;
    }

    public Control getControl(final int position) {
        return this.controls.get(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case CONTROL_VIEW_GROUPED: {
                final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__grouped_control, parent, false);
                return new ControlGroupViewHolder(v);
            }
            case CONTROL_VIEW:
            default: {
                final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__control, parent, false);
                return new ControlViewHolder(v);
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Control control = this.controls.get(position);

        switch (holder.getItemViewType()) {
            case CONTROL_VIEW_GROUPED: {
                final ControlGroupViewHolder vh = (ControlGroupViewHolder) holder;
                vh.setText(control.getLabel());
                vh.bind(position, this);

                if (this.selectedPosition == position) {
                    vh.unSelect(listener);
                } else {
                    vh.unselectView();
                }

                break;
            }
            case CONTROL_VIEW:
            default: {
                final ControlViewHolder vh = (ControlViewHolder) holder;
                vh.setText(control.getLabel());
                vh.bind(control, this.listener);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return this.controls.get(position).getType().equals("group")
                ? CONTROL_VIEW_GROUPED
                : CONTROL_VIEW;
    }

    @Override
    public int getItemCount() {
        return this.controls.size();
    }
}
