package com.bakkenbaeck.token.model.local;


import android.content.Intent;

public class ActivityResultHolder {

    private final int requestCode;
    private final int resultCode;
    private final Intent intent;

    public ActivityResultHolder (final int requestCode, final int resultCode, final Intent intent) {
        this.requestCode = requestCode;
        this.resultCode = resultCode;
        this.intent = intent;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public int getResultCode() {
        return resultCode;
    }

    public Intent getIntent() {
        return intent;
    }
}
