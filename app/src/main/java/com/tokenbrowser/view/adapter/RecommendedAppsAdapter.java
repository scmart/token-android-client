package com.tokenbrowser.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.token.R;
import com.tokenbrowser.model.network.App;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;
import com.tokenbrowser.view.adapter.viewholder.RecommendedAppsViewHolder;

import java.util.List;

public class RecommendedAppsAdapter extends RecyclerView.Adapter<RecommendedAppsViewHolder> {

    private List<App> apps;
    private OnItemClickListener<App> listener;

    public RecommendedAppsAdapter(final List<App> apps) {
        this.apps = apps;
    }

    public void setOnItemClickListener(final OnItemClickListener<App> listener) {
        this.listener = listener;
    }

    public void setItems(final List<App> apps) {
        this.apps.clear();
        this.apps.addAll(apps);
        this.notifyDataSetChanged();
    }

    @Override
    public RecommendedAppsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__recommended_app, parent, false);
        return new RecommendedAppsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecommendedAppsViewHolder holder, int position) {
        final App app = this.apps.get(position);

        holder.setApp(app);
        holder.bind(app, this.listener);
    }

    @Override
    public int getItemCount() {
        return this.apps.size();
    }
}
