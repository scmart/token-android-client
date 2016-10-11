package com.bakkenbaeck.toshi.view.dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.bakkenbaeck.toshi.R;

public class ProgressDialog extends DialogFragment{

    public static ProgressDialog newInstance(){
        return new ProgressDialog();
    }

    private View view;

    @Override
    public Dialog onCreateDialog(Bundle state) {
        Dialog dialog = super.onCreateDialog(state);
        if(dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_sending, container, true);
        getDialog().setCanceledOnTouchOutside(false);

        return view;
    }

    @Override
    public void onDetach() {
        view = null;
        super.onDetach();
    }
}
