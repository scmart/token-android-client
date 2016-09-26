package com.bakkenbaeck.toshi.view.dialog;


import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.bakkenbaeck.toshi.R;

public class PhoneInputDialog extends DialogFragment {

    private Button cancelButton;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.DialogTheme));
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.dialog_phone_input, null);
        builder.setView(view);

        initViews(view);

        final Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    private void initViews(final View view) {
        this.cancelButton = (Button) view.findViewById(R.id.cancelButton);
        this.cancelButton.setOnClickListener(this.dismissDialog);
    }

    private final View.OnClickListener dismissDialog = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            dismiss();
        }
    };

}
