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

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class GridSpacingDecoration extends RecyclerView.ItemDecoration {

    private int spanCount;
    private int horizontalSpacing;
    private int verticalSpacing;

    public GridSpacingDecoration(final int spanCount, final int horizontalSpacing, final int verticalSpacing) {
        this.spanCount = spanCount;
        this.horizontalSpacing = horizontalSpacing;
        this.verticalSpacing = verticalSpacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // item position

        final int rest = (position + 1) % spanCount;

        if (rest == 2) {
            outRect.right = horizontalSpacing / 2;
            outRect.left = horizontalSpacing / 2;
        } else if (rest == 1) {
            outRect.right = horizontalSpacing / 2;
        } else if (rest == 0) {
            outRect.left = horizontalSpacing / 2;
        }

        outRect.top = verticalSpacing;
    }
}
