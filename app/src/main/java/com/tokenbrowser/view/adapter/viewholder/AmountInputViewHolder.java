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
import android.widget.TextView;

import com.tokenbrowser.R;
import com.tokenbrowser.view.adapter.AmountInputAdapter;

public class AmountInputViewHolder extends RecyclerView.ViewHolder {
    private TextView label;
    private char value;
    private View divider;

    public AmountInputViewHolder(View itemView) {
        super(itemView);

        this.label = (TextView) itemView.findViewById(R.id.value);
        this.divider = itemView.findViewById(R.id.divider);
    }

    public void setText(final char value) {
        this.value = value;
        this.label.setText(String.valueOf(value));
    }

    public void bind(final AmountInputAdapter.OnKeyboardItemClicked listener) {
        if (listener == null) {
            return;
        }

        this.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onValueClicked(value);
            }
        });
    }

    public void hideDivider() {
        this.divider.setVisibility(View.GONE);
    }
}
