package com.bakkenbaeck.token.view.custom;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class RightSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private int horizontalSpacing;

    public RightSpaceItemDecoration(final int horizontalSpacing) {
        this.horizontalSpacing = horizontalSpacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        final int position = parent.getChildAdapterPosition(view);
        final int size = parent.getAdapter().getItemCount() - 1;

        if (position < size) {
            outRect.right = horizontalSpacing;
        }
    }
}
