package com.bakkenbaeck.token.view.adapter.viewholder;


import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bakkenbaeck.token.R;

public class ContactViewHolder extends ClickableViewHolder {

    public ImageView avatar;
    public TextView name;

    public ContactViewHolder(final View view) {
        super(view);
        this.name = (TextView) view.findViewById(R.id.name);
        this.avatar = (ImageView) view.findViewById(R.id.avatar);
    }

}