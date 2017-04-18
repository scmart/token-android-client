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

import com.tokenbrowser.model.local.Library;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;

public class LibraryViewHolder extends RecyclerView.ViewHolder {
    private TextView name;

    public LibraryViewHolder(View itemView) {
        super(itemView);

        this.name = (TextView) itemView;
    }

    public void setLibrary(final Library library) {
        this.name.setText(library.getName());
    }

    public void bind(final Library library, final OnItemClickListener<Library> listener) {
        this.name.setOnClickListener(view -> {
            if (listener == null) {
                return;
            }

            listener.onItemClick(library);
        });
    }
}
