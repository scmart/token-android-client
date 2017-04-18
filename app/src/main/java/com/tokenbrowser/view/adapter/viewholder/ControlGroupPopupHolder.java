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