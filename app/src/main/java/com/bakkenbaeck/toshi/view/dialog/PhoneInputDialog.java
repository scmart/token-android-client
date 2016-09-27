package com.bakkenbaeck.toshi.view.dialog;


import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.bakkenbaeck.toshi.R;
import com.bakkenbaeck.toshi.network.ws.model.VerificationStart;
import com.bakkenbaeck.toshi.view.BaseApplication;
import com.hbb20.CountryCodePicker;

public class PhoneInputDialog extends DialogFragment {

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
        view.findViewById(R.id.cancelButton).setOnClickListener(this.dismissDialog);
        view.findViewById(R.id.continueButton).setOnClickListener(new ValidateAndContinueDialog(view));
    }

    private final View.OnClickListener dismissDialog = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            dismiss();
        }
    };

    private class ValidateAndContinueDialog implements View.OnClickListener {

        private final View view;

        private ValidateAndContinueDialog(final View view) {
            this.view = view;
        }

        @Override
        public void onClick(final View v) {
            final EditText phoneNumberField = (EditText) this.view.findViewById(R.id.phone_number);
            if (TextUtils.isEmpty(phoneNumberField.getText())) {
                phoneNumberField.requestFocus();
                phoneNumberField.setError(getString(R.string.error__invalid_phone_number));
                return;
            }

            final String countryCode = ((CountryCodePicker)view.findViewById(R.id.country_code)).getSelectedCountryCodeWithPlus();
            final String fullNumber = countryCode + phoneNumberField.getText();

            final VerificationStart vsFrame = new VerificationStart(fullNumber);
            BaseApplication.get().sendWebSocketMessage(vsFrame.toString());
        }
    }

}
