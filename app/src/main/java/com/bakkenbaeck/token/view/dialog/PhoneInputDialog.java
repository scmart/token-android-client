package com.bakkenbaeck.token.view.dialog;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.network.rest.model.VerificationSent;
import com.bakkenbaeck.token.network.ws.model.VerificationStart;
import com.bakkenbaeck.token.network.ws.model.WebSocketError;
import com.bakkenbaeck.token.network.ws.model.WebSocketErrors;
import com.bakkenbaeck.token.util.KeyboardUtil;
import com.bakkenbaeck.token.util.LocaleUtil;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.util.SnackbarUtil;
import com.bakkenbaeck.token.view.BaseApplication;
import com.hbb20.CountryCodePicker;

import java.util.Locale;

import rx.Observable;
import rx.subjects.PublishSubject;

public class PhoneInputDialog extends DialogFragment {
    private static final String TAG = "PhoneInputDialog";
    private String inputtedPhoneNumber;
    private Listener listener;
    private View view;
    private boolean visible;

    private OnNextSubscriber<WebSocketError> errorSubscriber;
    private OnNextSubscriber<VerificationSent> verificationSentSubscriber;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface Listener {
        void onPhoneInputSuccess(final PhoneInputDialog dialog);
    }

    public String getInputtedPhoneNumber() {
        return this.inputtedPhoneNumber;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        try {
            this.listener = (PhoneInputDialog.Listener) context;
        } catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement PhoneInputDialog.Listener");
        }

        this.errorSubscriber = generateErrorSubscriber();
        this.verificationSentSubscriber = generateVerificationSentSubscriber();
        BaseApplication.get().getSocketObservables().getErrorObservable().subscribe(this.errorSubscriber);
        BaseApplication.get().getSocketObservables().getVerificationSentObservable().subscribe(this.verificationSentSubscriber);

        visible = true;
    }

    private OnNextSubscriber<WebSocketError> generateErrorSubscriber() {
        return new OnNextSubscriber<WebSocketError>() {
            @Override
            public void onNext(final WebSocketError webSocketError) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        view.findViewById(R.id.spinner_view).setVisibility(View.INVISIBLE);
                        setErrorOnPhoneField(webSocketError);
                    }
                });
            }
        };
    }

    private OnNextSubscriber<VerificationSent> generateVerificationSentSubscriber() {
        return new OnNextSubscriber<VerificationSent>() {
            @Override
            public void onNext(final VerificationSent verificationSent) {
                Log.d(TAG, "onNext: generateVerificationSentSubscriber");
                listener.onPhoneInputSuccess(PhoneInputDialog.this);
                dismiss();
            }
        };
    }

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
        view = inflater.inflate(R.layout.dialog_phone_input, container, true);
        getDialog().setCanceledOnTouchOutside(false);

        initViews(view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        EditText phoneNumber = (EditText) view.findViewById(R.id.phone_number);
        phoneNumber.requestFocus();
        if(getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void initViews(final View view) {
        final Locale currentLocale = LocaleUtil.getLocale();
        ((CountryCodePicker)view.findViewById(R.id.country_code)).setCountryForNameCode(currentLocale.getCountry());
        view.findViewById(R.id.cancelButton).setOnClickListener(this.dismissDialog);
        view.findViewById(R.id.continueButton).setOnClickListener(new PhoneInputDialog.ValidateAndContinueDialog(view));

        EditText phoneNumber = (EditText) view.findViewById(R.id.phone_number);
        phoneNumber.requestFocus();
        if(getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private final View.OnClickListener dismissDialog = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            EditText phoneNumber = (EditText) view.findViewById(R.id.phone_number);
            if(phoneNumber != null) {
                phoneNumber.requestFocus();
                KeyboardUtil.showKeyboard(getActivity(), phoneNumber, true);
            }
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
                setErrorOnPhoneField();
                return;
            }

            showError(false, "");

            final String countryCode = ((CountryCodePicker)view.findViewById(R.id.country_code)).getSelectedCountryCodeWithPlus();
            inputtedPhoneNumber = countryCode + phoneNumberField.getText();

            final VerificationStart vsFrame = new VerificationStart(inputtedPhoneNumber);
            BaseApplication.get().sendWebSocketMessage(vsFrame.toString());

            this.view.findViewById(R.id.spinner_view).setVisibility(View.VISIBLE);
        }
    }

    private void setErrorOnPhoneField() {
        setErrorOnPhoneField(null);
    }

    private void setErrorOnPhoneField(final WebSocketError error) {
        final EditText phoneNumberField = (EditText) this.view.findViewById(R.id.phone_number);
        phoneNumberField.requestFocus();

        if (error != null && error.getCode().equals(WebSocketErrors.phone_number_already_in_use)) {
            String errorMessage = getContext().getResources().getString(R.string.error__phone_number_in_use);
            showError(true, errorMessage);

        } else {
            String errorMessage = getContext().getResources().getString(R.string.error__invalid_phone_number);
            showError(true, errorMessage);
            phoneNumberField.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.edit_text_underline_error_state));
        }
    }

    private void showError(boolean show, String errorMessage){
        if(this.view != null) {
            final TextView phoneNumberError = (TextView) this.view.findViewById(R.id.phone_number_error);
            final EditText phoneNumberField = (EditText) this.view.findViewById(R.id.phone_number);

            if(phoneNumberError != null) {
                if (show) {
                    phoneNumberError.setVisibility(View.VISIBLE);
                    phoneNumberError.setText(errorMessage);
                    if(phoneNumberField != null) {
                        phoneNumberField.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.edit_text_underline_error_state));
                    }
                } else {
                    phoneNumberError.setVisibility(View.INVISIBLE);
                    phoneNumberError.setText(errorMessage);
                }
            }
        }
    }

    @Override
    public void onDetach() {
        this.errorSubscriber.unsubscribe();
        this.verificationSentSubscriber.unsubscribe();
        this.errorSubscriber = null;
        this.verificationSentSubscriber = null;
        visible = false;
        super.onDetach();
    }

    public boolean isVisible2(){
        return visible;
    }

}
