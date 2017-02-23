package com.bakkenbaeck.token.view.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bakkenbaeck.token.R;

public class RecyclerViewDivider extends RecyclerView.ItemDecoration {
    private Drawable mDivider;
    private int padding;

    public RecyclerViewDivider(final Context context, final int padding) {
        mDivider = ContextCompat.getDrawable(context, R.drawable.decoration_linear_divider);
        this.padding = padding;
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        final int left = parent.getPaddingLeft() + padding;
        final int right = parent.getWidth() - parent.getPaddingRight() - padding;

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}