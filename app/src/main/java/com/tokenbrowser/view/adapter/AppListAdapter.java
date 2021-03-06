/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.R;
import com.tokenbrowser.model.network.App;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;
import com.tokenbrowser.view.adapter.viewholder.AppListViewHolder;

import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListViewHolder> {

    private List<App> apps;
    private OnItemClickListener<App> listener;

    public AppListAdapter(final List<App> apps) {
        this.apps = apps;
    }

    public void setOnItemClickListener(final OnItemClickListener<App> listener) {
        this.listener = listener;
    }

    public void setApps(final List<App> apps) {
        this.apps.clear();
        this.apps.addAll(apps);
        this.notifyDataSetChanged();
    }

    @Override
    public AppListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__app_item, parent, false);
        return new AppListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AppListViewHolder holder, int position) {
        final App app = this.apps.get(position);

        holder.setApp(app);
        holder.bind(app, listener);
    }

    @Override
    public int getItemCount() {
        return this.apps.size();
    }
}
