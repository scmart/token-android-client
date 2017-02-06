package com.bakkenbaeck.token.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.adapter.viewholder.RecommendedAppsViewHolder;

import java.util.List;

public class RecommendedAppsAdapter extends RecyclerView.Adapter<RecommendedAppsViewHolder> {

    private List<String> apps;

    public RecommendedAppsAdapter(final List<String> apps) {
        this.apps = apps;
    }

    @Override
    public RecommendedAppsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__recommended_app, parent, false);
        return new RecommendedAppsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecommendedAppsViewHolder holder, int position) {
        final String app = this.apps.get(position);

        holder.setLabel(app);
        holder.setCategory("Bot");
    }

    @Override
    public int getItemCount() {
        return this.apps.size();
    }
}
