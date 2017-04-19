package com.tokenbrowser.util;

import android.content.Context;
import android.widget.Toast;

public class TimedToast {

    private static final long REFRESH_LIMIT = 3000;
    private long lastShown = -1;
    private Toast toast;

    public TimedToast makeText(final Context context, final String text) {
        this.toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        return this;
    }

    public void show() {
        final long systemMillis = System.currentTimeMillis();
        final boolean idle = this.lastShown == -1;
        final boolean refreshable = systemMillis > this.lastShown + REFRESH_LIMIT;
        if (!idle && !refreshable) return;
        this.lastShown = systemMillis;
        this.toast.show();
    }
}
