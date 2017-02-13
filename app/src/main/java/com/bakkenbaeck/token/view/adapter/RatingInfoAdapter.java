package com.bakkenbaeck.token.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.adapter.viewholder.RatingInfoViewHolder;

public class RatingInfoAdapter extends RecyclerView.Adapter<RatingInfoViewHolder> {

    private static final int MAX_ITEMS = 5;

    @Override
    public RatingInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__rating_info, parent, false);
        return new RatingInfoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RatingInfoViewHolder holder, int position) {
        final String stars = String.valueOf(MAX_ITEMS - position);
        holder.setStars(stars);
        holder.setPercentageRating(99);
    }

    @Override
    public int getItemCount() {
        return MAX_ITEMS;
    }
}
