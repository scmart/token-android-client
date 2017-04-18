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

package com.tokenbrowser.view.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.tokenbrowser.model.network.ReputationScore;
import com.tokenbrowser.R;
import com.tokenbrowser.view.adapter.RatingInfoAdapter;

public class RatingInfo extends RecyclerView {

    public RatingInfo(Context context) {
        super(context);
        init();
    }

    public RatingInfo(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RatingInfo(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_rating_info, this);
        initRecyclerView();
    }

    private void initRecyclerView() {
        this.setLayoutManager(new LinearLayoutManager(getContext()));
        this.setAdapter(new RatingInfoAdapter());
    }

    public void setRatingInfo(final ReputationScore reputationScore) {
        final RatingInfoAdapter adapter = (RatingInfoAdapter) this.getAdapter();
        adapter.setReputationScore(reputationScore);
    }
}
