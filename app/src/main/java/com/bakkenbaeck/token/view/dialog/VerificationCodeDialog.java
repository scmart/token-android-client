package com.bakkenbaeck.token.view.dialog;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.network.ws.model.VerificationConfirm;
import com.bakkenbaeck.token.network.ws.model.VerificationSuccess;
import com.bakkenbaeck.token.network.ws.model.WebSocketError;
import com.bakkenbaeck.token.util.KeyboardUtil;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.view.BaseApplication;

public class VerificationCodeDialog extends DialogFragment {
    private static final String TAG = "VerificationCodeDialog";

    private static final String PHONE_NUMBER = "phone_number";
    private String phoneNumber;
    private Listener listener;
    private View view;

    private OnNextSubscriber<WebSocketError> errorSubscriber;
    private OnNextSubscriber<VerificationSuccess> successSubscriber;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface Listener {
        void onVerificationCodeSuccess(final VerificationCodeDialog dialog);
    }


    public static VerificationCodeDialog newInstance(final String phoneNumber) {
        final VerificationCodeDialog dialog = new VerificationCodeDialog();
        final Bundle args = new Bundle();
        args.putString(PHONE_NUMBER, phoneNumber);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        try {
            this.listener = (Listener) context;
        } catch (final ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement VerificationCodeDialog.Listener");
        }

        this.errorSubscriber = generateErrorSubscriber();
        this.successSubscriber = generateSuccessSubscriber();
        BaseApplication.get().getSocketObservables().getErrorObservable().subscribe(this.errorSubscriber);
        BaseApplication.get().getSocketObservables().getVerificationSuccessObservable().subscribe(this.successSubscriber);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle state) {
        Dialog dialog = super.onCreateDialog(state);
        if(dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        this.phoneNumber = getArguments().getString(PHONE_NUMBER);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_verification_code, container, true);
        getDialog().setCanceledOnTouchOutside(false);

        initViews(view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        EditText verificationCode = (EditText) view.findViewById(R.id.verification_code);
        verificationCode.requestFocus();
        if(getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private OnNextSubscriber<WebSocketError> generateErrorSubscriber() {
        return new OnNextSubscriber<WebSocketError>() {
            @Override
            public void onNext(final WebSocketError webSocketError) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        view.findViewById(R.id.spinner_view).setVisibility(View.INVISIBLE);
                        setErrorOnCodeField();
                    }
                });
            }
        };
    }

    private OnNextSubscriber<VerificationSuccess> generateSuccessSubscriber() {
        return new OnNextSubscriber<VerificationSuccess>() {
            @Override
            public void onNext(final VerificationSuccess verificationSuccess) {
                // Update the user
                BaseApplication.get().getUserManager().refresh();
                listener.onVerificationCodeSuccess(VerificationCodeDialog.this);
                dismiss();
            }
        };
    }

    private void initViews(final View view) {
        view.findViewById(R.id.cancelButton).setOnClickListener(this.dismissDialog);
        view.findViewById(R.id.continueButton).setOnClickListener(new ValidateAndContinueDialog(view));
    }

    private final View.OnClickListener dismissDialog = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            EditText verificationCode = (EditText) view.findViewById(R.id.verification_code);
            if(verificationCode != null) {
                verificationCode.requestFocus();
                KeyboardUtil.showKeyboard(getActivity(), verificationCode, false);
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
            final EditText verificationCodeInput = (EditText) this.view.findViewById(R.id.verification_code);
            if (TextUtils.isEmpty(verificationCodeInput.getText())) {
                setErrorOnCodeField();
                return;
            }

            showError(false, "");

            final String inputtedVerificationCode = verificationCodeInput.getText().toString().trim();

            final VerificationConfirm vcFrame = new VerificationConfirm(phoneNumber, inputtedVerificationCode);
            BaseApplication.get().sendWebSocketMessage(vcFrame.toString());

            this.view.findViewById(R.id.spinner_view).setVisibility(View.VISIBLE);
        }
    }

    private void setErrorOnCodeField() {
        final EditText verificationCodeInput = (EditText) this.view.findViewById(R.id.verification_code);
        verificationCodeInput.requestFocus();
        String errorMessage = getContext().getResources().getString(R.string.error__invalid_verification_code);
        showError(true, errorMessage);
    }

    private void showError(boolean show, String errorMessage){
        if(this.view != null) {
            final TextView phoneNumberError = (TextView) this.view.findViewById(R.id.verification_code_error);
            final EditText phoneNumberField = (EditText) this.view.findViewById(R.id.verification_code);

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
        this.successSubscriber.unsubscribe();
        this.errorSubscriber = null;
        this.successSubscriber = null;
        super.onDetach();
    }
}
