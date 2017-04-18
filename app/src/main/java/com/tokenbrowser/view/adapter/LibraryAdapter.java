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
import com.tokenbrowser.model.local.Library;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;
import com.tokenbrowser.view.adapter.viewholder.LibraryViewHolder;

import java.util.List;

public class LibraryAdapter extends RecyclerView.Adapter<LibraryViewHolder> {

    private List<Library> libraries;
    private OnItemClickListener<Library> listener;

    public LibraryAdapter(final List<Library> libraries) {
        this.libraries = libraries;
    }

    public void setOnItemClickListener(final OnItemClickListener<Library> listener) {
        this.listener = listener;
    }

    public void setLibraries(final List<Library> libraries) {
        this.libraries.addAll(libraries);
        this.notifyDataSetChanged();
    }

    @Override
    public LibraryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__library, parent, false);
        return new LibraryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(LibraryViewHolder holder, int position) {
        final Library library = this.libraries.get(position);
        holder.setLibrary(library);
        holder.bind(library, listener);
    }

    @Override
    public int getItemCount() {
        return this.libraries.size();
    }
}
