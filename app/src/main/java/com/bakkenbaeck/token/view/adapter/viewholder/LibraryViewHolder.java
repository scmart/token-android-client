package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.token.model.local.Library;
import com.bakkenbaeck.token.view.adapter.listeners.OnItemClickListener;

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
