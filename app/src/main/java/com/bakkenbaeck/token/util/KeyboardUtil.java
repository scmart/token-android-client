package com.bakkenbaeck.token.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class KeyboardUtil {

    public static void showKeyboard(Activity activity, View v, boolean b){
        InputMethodManager imm =(InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
        if(b) {
            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
        } else{
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}
