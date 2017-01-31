package com.bakkenbaeck.token.view.custom;

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
