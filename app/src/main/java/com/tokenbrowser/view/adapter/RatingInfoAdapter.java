package com.tokenbrowser.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.model.network.Stars;
import com.tokenbrowser.token.R;
import com.tokenbrowser.view.adapter.viewholder.RatingInfoViewHolder;

public class RatingInfoAdapter extends RecyclerView.Adapter<RatingInfoViewHolder> {

    private static final int MAX_ITEMS = 5;

    private Stars starInfo;

    public void setStarInfo(final Stars starInfo) {
        this.starInfo = starInfo;
        this.notifyDataSetChanged();
    }

    @Override
    public RatingInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__rating_info, parent, false);
        return new RatingInfoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RatingInfoViewHolder holder, int position) {
        final String stars = String.valueOf(MAX_ITEMS - position);
        holder.setStars(stars);

        if (this.starInfo == null) {
            holder.setPercentageRating(0);
            return;
        }

        final int rating = this.starInfo.getAmountOfOneStarRatings(position + 1);
        holder.setPercentageRating(rating);
    }

    @Override
    public int getItemCount() {
        return MAX_ITEMS;
    }
}
