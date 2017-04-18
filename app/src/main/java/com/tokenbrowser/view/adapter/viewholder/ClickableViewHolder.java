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

public class ClickableViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public interface OnClickListener {
        void onClick(int position);
    }

    private OnClickListener onClickListener;

    public ClickableViewHolder(final View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
    }


    @Override
    public void onClick(final View v) {
        if (this.onClickListener != null) {
            this.onClickListener.onClick(getAdapterPosition());
        }
    }

    public void setOnClickListener(final OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
