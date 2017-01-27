package com.bakkenbaeck.token.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.adapter.viewholder.AppListViewHolder;

import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListViewHolder> {

    private List<String> apps;

    public AppListAdapter(final List<String> apps) {
        this.apps = apps;
    }

    @Override
    public AppListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__app_item, parent, false);
        return new AppListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AppListViewHolder holder, int position) {
        final String app = this.apps.get(position);

        holder.setAppName(app);
        holder.setAppImage(R.drawable.green_circle);
    }

    @Override
    public int getItemCount() {
        return this.apps.size();
    }
}
