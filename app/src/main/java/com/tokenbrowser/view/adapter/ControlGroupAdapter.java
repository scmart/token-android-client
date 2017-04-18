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
import com.tokenbrowser.model.sofa.SofaType;
import com.tokenbrowser.view.adapter.viewholder.ControlGroupPopupHolder;

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

        final boolean actionIsNotNull = control.getAction() != null;
        if (actionIsNotNull && control.getAction().toLowerCase().contains(SofaType.WEB_VIEW)) {
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