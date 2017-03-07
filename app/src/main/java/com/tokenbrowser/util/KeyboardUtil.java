package com.tokenbrowser.util;


import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.tokenbrowser.view.BaseApplication;

public class KeyboardUtil {

    public static void hideKeyboard(final View view) {
        ((InputMethodManager) BaseApplication
                .get()
                .getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
