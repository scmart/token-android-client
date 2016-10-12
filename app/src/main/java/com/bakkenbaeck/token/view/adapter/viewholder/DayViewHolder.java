package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.token.R;

public class DayViewHolder extends RecyclerView.ViewHolder{
    public TextView date;

    public DayViewHolder(View v) {
        super(v);

        date = (TextView) v.findViewById(R.id.date);
    }
}
