package com.tokenbrowser.view.custom;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class HorizontalLineDivider extends RecyclerView.ItemDecoration {

    private final Paint paint;
    private int leftPadding;
    private int dividerHeight = 6;

    public HorizontalLineDivider(final int color) {
        this.paint = new Paint();
        this.paint.setColor(color);
    }

    public HorizontalLineDivider setLeftPadding(final int padding) {
        this.leftPadding = padding;
        return this;
    }

    public HorizontalLineDivider setDividerHeight(final int dividerHeight) {
        this.dividerHeight = dividerHeight;
        return this;
    }

    @Override
    public void onDraw(final Canvas canvas,
                       final RecyclerView parent,
                       final RecyclerView.State state) {

        final int dividerLeft = parent.getPaddingLeft() + leftPadding;
        final int dividerRight = parent.getWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();

        for (int i = 0; i < childCount - 1; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            final int dividerTop = child.getBottom() + params.bottomMargin - (this.dividerHeight / 2);
            final int dividerBottom = dividerTop + (this.dividerHeight / 2);

            canvas.drawRect(dividerLeft, dividerTop, dividerRight, dividerBottom, paint);
        }
    }
}