package com.tokenbrowser.view.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class ControlRecyclerView extends RecyclerView {

    public interface OnSizeChangedListener {
        void onSizeChanged(final int height);
    }

    private OnSizeChangedListener listener;

    public ControlRecyclerView(Context context) {
        super(context);
    }

    public ControlRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ControlRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnSizedChangedListener(final OnSizeChangedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (listener == null || getHeight() == 0) {
            return;
        }

        listener.onSizeChanged(getHeight());
    }
}
