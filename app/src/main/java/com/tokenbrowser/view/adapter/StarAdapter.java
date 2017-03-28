package com.tokenbrowser.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.R;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;
import com.tokenbrowser.view.adapter.viewholder.StarViewHolder;

public class StarAdapter extends RecyclerView.Adapter<StarViewHolder> {

    private static final int MAX_STARS = 5;
    private double rating;
    private boolean bigMode;
    private OnItemClickListener<Integer> listener;

    public void setOnItemClickListener(final OnItemClickListener<Integer> listener) {
        this.listener = listener;
    }

    public StarAdapter(final boolean size) {
        this.bigMode = size;
    }

    public void setStars(final double rating) {
        this.rating = rating;
        this.notifyDataSetChanged();
    }

    @Override
    public StarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = bigMode
                ? LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__star_big, parent, false)
                : LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__star_small, parent, false);
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

        if (this.bigMode && this.listener != null) {
            holder.bind(position, this);
        }
    }

    public void updateSelectedStars(final int starsSelected) {
        this.rating = starsSelected;
        this.notifyDataSetChanged();
        this.listener.onItemClick(starsSelected);
    }

    @Override
    public int getItemCount() {
        return MAX_STARS;
    }
}