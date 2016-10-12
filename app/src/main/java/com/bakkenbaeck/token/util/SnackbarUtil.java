package com.bakkenbaeck.token.util;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

public class SnackbarUtil {

    public static Snackbar make(View root, String message){
        Snackbar snack = Snackbar.make(root, message, Snackbar.LENGTH_SHORT);
        View view = snack.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);

        return snack;
    }
}
