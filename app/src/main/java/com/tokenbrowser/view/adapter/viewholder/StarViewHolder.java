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

import com.tokenbrowser.R;
import com.tokenbrowser.view.adapter.StarAdapter;

public class StarViewHolder extends RecyclerView.ViewHolder {
    private ImageView imageView;

    public StarViewHolder(View itemView) {
        super(itemView);

        imageView = (ImageView) itemView.findViewById(R.id.star);
    }

    public void setWholeStar() {
        imageView.setImageResource(R.drawable.star);
    }

    public void setHalfStar() {
        imageView.setImageResource(R.drawable.star_half);
    }

    public void setWholeGreyStar() {
        imageView.setImageResource(R.drawable.star_grey);
    }

    public void bind(final int position, final StarAdapter adapter) {
        this.itemView.setOnClickListener(v -> adapter.updateSelectedStars(position + 1));
    }
}
