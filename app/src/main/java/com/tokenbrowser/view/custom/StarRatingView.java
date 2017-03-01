package com.tokenbrowser.view.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.tokenbrowser.token.R;
import com.tokenbrowser.view.adapter.StarAdapter;

public class StarRatingView extends RecyclerView {

    public StarRatingView(Context context) {
        super(context);
        init();
    }

    public StarRatingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StarRatingView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_star_rating, this);
        initRecyclerView();
    }

    private void initRecyclerView() {
        final int spacing = this.getResources().getDimensionPixelSize(R.dimen.star_right_margin);
        this.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        this.addItemDecoration(new SpaceDecoration(spacing));
        this.setAdapter(new StarAdapter());
    }

    public void setStars(final double rating) {
        if (this.getAdapter() == null) {
            return;
        }

        final StarAdapter adapter = (StarAdapter) this.getAdapter();
        adapter.setStars(rating);
    }
}