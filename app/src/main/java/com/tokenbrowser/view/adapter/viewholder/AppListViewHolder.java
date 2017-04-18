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

package com.tokenbrowser.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tokenbrowser.R;
import com.tokenbrowser.model.network.App;
import com.tokenbrowser.util.ImageUtil;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;

public class AppListViewHolder extends RecyclerView.ViewHolder {
    private ImageView appImage;
    private TextView appName;

    public AppListViewHolder(View itemView) {
        super(itemView);

        this.appImage = (ImageView) itemView.findViewById(R.id.app_image);
        this.appName = (TextView) itemView.findViewById(R.id.app_name);
    }

    public void setApp(final App app) {
        this.appName.setText(app.getCustom().getName());
        ImageUtil.loadFromNetwork(app.getCustom().getAvatar(), this.appImage);
    }

    public void bind(final App app, final OnItemClickListener<App> listener) {
        this.itemView.setOnClickListener(view -> {
            if (listener == null) {
                return;
            }

            listener.onItemClick(app);
        });
    }
}
