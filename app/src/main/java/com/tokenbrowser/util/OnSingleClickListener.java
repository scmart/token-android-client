package com.tokenbrowser.util;


import android.view.View;

public abstract class OnSingleClickListener implements View.OnClickListener {

    private static final long MIN_DELAY_MS = 500;

    private long mLastClickTime;

    @Override
    public final void onClick(View v) {
        long lastClickTime = mLastClickTime;
        long now = System.currentTimeMillis();
        mLastClickTime = now;
        if (now - lastClickTime >= MIN_DELAY_MS) {
            onSingleClick(v);
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    public abstract void onSingleClick(View v);
}
