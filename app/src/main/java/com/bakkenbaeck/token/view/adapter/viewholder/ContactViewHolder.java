package com.bakkenbaeck.token.view.adapter.viewholder;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bakkenbaeck.token.R;

public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


    public ImageView avatar;
    public TextView name;

    public ContactViewHolder(final View view) {
        super(view);
        this.name = (TextView) view.findViewById(R.id.name);
        this.avatar = (ImageView) view.findViewById(R.id.avatar);
        view.setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        Toast.makeText(v.getContext(), String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
    }
}