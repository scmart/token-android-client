package com.tokenbrowser.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.token.R;
import com.tokenbrowser.view.adapter.viewholder.StarViewHolder;

public class StarAdapter extends RecyclerView.Adapter<StarViewHolder> {

    private static final int MAX_STARS = 5;
    private double rating;

    public void setStars(final double rating) {
        this.rating = rating;
        this.notifyDataSetChanged();
    }

    @Override
    public StarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__star, parent, false);
        return new StarViewHolder(v);
    }

    @Override
    public void onBindViewHolder(StarViewHolder holder, int position) {
        final double rest = this.rating - position;

        if (rest < 1 && rest > 0) {
            holder.setHalfStar();
        } else if (position < this.rating){
            holder.setWholeStar();
        } else {
            holder.setWholeGreyStar();
        }
    }

    @Override
    public int getItemCount() {
        return MAX_STARS;
    }
}