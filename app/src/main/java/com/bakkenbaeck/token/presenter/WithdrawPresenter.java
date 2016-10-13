package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.ActivityResultHolder;
import com.bakkenbaeck.token.model.LocalBalance;
import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.network.rest.TokenService;
import com.bakkenbaeck.token.network.rest.model.SignatureRequest;
import com.bakkenbaeck.token.network.rest.model.SignedWithdrawalRequest;
import com.bakkenbaeck.token.network.rest.model.TransactionSent;
import com.bakkenbaeck.token.network.rest.model.WithdrawalRequest;
import com.bakkenbaeck.token.network.ws.model.TransactionConfirmation;
import com.bakkenbaeck.token.util.EthUtil;
import com.bakkenbaeck.token.util.LocaleUtil;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.util.OnNextObserver;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.util.RetryWithBackoff;
import com.bakkenbaeck.token.util.SnackbarUtil;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.BarcodeScannerActivity;
import com.bakkenbaeck.token.view.activity.WithdrawActivity;
import com.bakkenbaeck.token.view.adapter.PreviousWalletAddress;
import com.bakkenbaeck.token.view.dialog.ProgressDialog;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;

import retrofit2.Response;
import rx.Subscriber;

import static android.app.Activity.RESULT_OK;

public class WithdrawPresenter implements Presenter<WithdrawActivity> {
    private static final String TAG = "WithdrawPresenter";
    static final String INTENT_WALLET_ADDRESS = "wallet_address";
    static final String INTENT_WITHDRAW_AMOUNT = "withdraw_amount";

    private WithdrawActivity activity;
    private User currentUser;
    private boolean firstTimeAttaching = true;
    private BigDecimal currentBalance = BigDecimal.ZERO;
    private ProgressDialog progressDialog;
    private final BigDecimal minWithdrawLimit = new BigDecimal("0.0000000001");

    private final PreviousWalletAddress previousWalletAddress = new PreviousWalletAddress();

    @Override
    public void onViewAttached(final WithdrawActivity activity) {
        this.activity = activity;
        initButtons();
        initToolbar();
        initPreviousAddress();

        if (firstTimeAttaching) {
            firstTimeAttaching = false;
            registerObservables();
            initProgressDialog2();
        }
    }

    private void initProgressDialog2(){
        progressDialog = ProgressDialog.newInstance();
    }

    private void initButtons() {
        this.activity.getBinding().barcodeButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(final View v) {
                showBarcodeActivity();
            }
        });

        this.activity.getBinding().sendButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(final View view) {
                handleSendClicked();
            }
        });

        this.activity.getBinding().walletAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {}

            @Override
            public void onTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {
                updateSendButtonEnabledState();
            }

            @Override
            public void afterTextChanged(final Editable editable) {}
        });

        this.activity.getBinding().amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSendButtonEnabledState();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //refreshButtonStates();
    }

    private void updateSendButtonEnabledState() {
        final Editable walletAddress = this.activity.getBinding().walletAddress.getText();
        String amount = this.activity.getBinding().amount.getText().toString();
        final boolean shouldEnableButton = walletAddress.length() > 0 && userHasEnoughReputationScore() && amount.length() > 0;
        enableSendButton(shouldEnableButton);
        activity.getBinding().sendButton.setEnabled(shouldEnableButton);
    }

    private void enableSendButton(boolean enabled){
        if(enabled){
            activity.getBinding().sendButton.setTextColor(Color.parseColor("#FFFFFF"));
            activity.getBinding().sendButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.btn_with_radius));
        }else{
            activity.getBinding().sendButton.setTextColor(Color.parseColor("#33565A64"));
            activity.getBinding().sendButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.disabled_background));
        }
    }

    private void initToolbar() {
        final String title = this.activity.getResources().getString(R.string.withdraw__title);
        final Toolbar toolbar = this.activity.getBinding().toolbar;
        this.activity.setSupportActionBar(toolbar);
        this.activity.getSupportActionBar().setTitle(title);
        this.activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initPreviousAddress() {
        final EditText walletAddress = this.activity.getBinding().walletAddress;
        walletAddress.setText(this.previousWalletAddress.getAddress());
        walletAddress.setSelection(walletAddress.getText().length());
    }

    private final OnNextObserver<LocalBalance> newBalanceSubscriber = new OnNextObserver<LocalBalance>() {
        @Override
        public void onNext(final LocalBalance newBalance) {
            if (activity != null && newBalance != null) {
                activity.getBinding().balanceBar.setBalance(newBalance.unconfirmedBalanceString());
                currentBalance = newBalance.getConfirmedBalanceAsEthMinusTransferFee();
                tryPopulateAmountField(currentBalance, newBalance.confirmedBalanceStringMinusTransferFee());
            }
        }
    };

    private final OnNextObserver<Integer> newReputationSubscriber = new OnNextObserver<Integer>() {
        @Override
        public void onNext(Integer reputationScore) {
            if(activity != null){
                activity.getBinding().balanceBar.setReputation(reputationScore);
            }
        }
    };

    private void tryPopulateAmountField(final BigDecimal previousBalance, final String newBalanceAsEthString) {
        try {
            String s = this.activity.getBinding().amount.getText().toString();
            if (new BigDecimal(s).equals(previousBalance)) {
                this.activity.getBinding().amount.setText(newBalanceAsEthString);
                this.activity.getBinding().amount.setSelection(this.activity.getBinding().amount.getText().length());
            }
        } catch (final Exception ex) {
            // Do nothing -- user is editing the field
        }

    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        this.activity = null;
    }

    public void handleActivityResult(final ActivityResultHolder activityResultHolder) {
        if (activityResultHolder.getResultCode() != RESULT_OK) {
            return;
        }

        tryParseBarcodeResult(activityResultHolder);
    }

    private void showBarcodeActivity() {
        new IntentIntegrator(this.activity)
                .setCaptureActivity(BarcodeScannerActivity.class)
                .setOrientationLocked(true)
                .setPrompt("")
                .setBeepEnabled(true)
                .initiateScan();
    }

    private void tryParseBarcodeResult(final ActivityResultHolder activityResultHolder) {
        final IntentResult result = IntentIntegrator.parseActivityResult(
                activityResultHolder.getRequestCode(),
                activityResultHolder.getResultCode(),
                activityResultHolder.getIntent());
        if(result == null || result.getContents() == null) {
            return;
        }

        this.activity.getBinding().walletAddress.setText(result.getContents());
    }

    private void handleSendClicked() {
        if (!validate()) {
            return;
        }

        try {
            final NumberFormat nf = NumberFormat.getInstance(LocaleUtil.getLocale());
            final String inputtedText = this.activity.getBinding().amount.getText().toString();
            String parsedInput = inputtedText.replace(".", ",");
            final BigDecimal amountInEth = new BigDecimal(nf.parse(parsedInput).toString());

            final BigInteger amountInWei = EthUtil.ethToWei(amountInEth);
            final String toAddress = this.activity.getBinding().walletAddress.getText().toString();

            final WithdrawalRequest withdrawalRequest = new WithdrawalRequest(amountInWei, toAddress);
            TokenService.getApi()
                    .postWithdrawalRequest(this.currentUser.getAuthToken(), withdrawalRequest)
                    .subscribe(generateSigningSubscriber());
            progressDialog.show(this.activity.getSupportFragmentManager(), "progressDialog");
            this.previousWalletAddress.setAddress(toAddress);
        } catch (final ParseException ex) {
            LogUtil.e(getClass(), ex.toString());
        }
    }

    private Subscriber<Response<SignatureRequest>> generateSigningSubscriber() {
        return new Subscriber<Response<SignatureRequest>>() {
            @Override
            public void onCompleted() {}

            @Override
            public void onError(final Throwable ex) {
                Log.d(TAG, "onError: 1 " + ex);
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(activity, "There was a problem withdrawing, please try again.", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                });
                LogUtil.e(getClass(), "postWithdrawalRequest: " + ex);
            }

            @Override
            public void onNext(final Response<SignatureRequest> signatureRequest) {
                Log.d(TAG, "onNext: 1 " + signatureRequest.code());
                final String unsignedTransaction = signatureRequest.body().getTransaction();
                final String signature = BaseApplication.get().getUserManager().signTransaction(unsignedTransaction);
                final SignedWithdrawalRequest request = new SignedWithdrawalRequest(unsignedTransaction, signature);

                TokenService.getApi()
                        .postSignedWithdrawal(currentUser.getAuthToken(), request)
                        .retryWhen(new RetryWithBackoff(5))
                        .subscribe(generateSignedWithdrawalSubscriber());

                if(signatureRequest.code() == 400){
                    SnackbarUtil.make(activity.getBinding().root, signatureRequest.body().getMessage());
                }
            }

            private Subscriber<Response<TransactionSent>> generateSignedWithdrawalSubscriber() {
                return new Subscriber<Response<TransactionSent>>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(final Throwable ex) {
                        Log.d(TAG, "onError: 2 " + ex);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                //Toast.makeText(activity, R.string.error__withdrawing, Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        });
                        LogUtil.e(getClass(), "postSignedWithdrawal: " + ex);
                    }

                    @Override
                    public void onNext(final Response<TransactionSent> transactionSent) {
                        Log.d(TAG, "onNext: 2 " + transactionSent.code());
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });

                        if(transactionSent.code() == 400){
                            SnackbarUtil.make(activity.getBinding().root, "Not enough funds to transfer the requested amount").show();
                            return;
                        }

                        BigInteger t1 = transactionSent.body().getConfirmedBalance();
                        BigInteger t2 = transactionSent.body().getUnconfirmedBalance();
                        TransactionConfirmation t = new TransactionConfirmation(t1.toString(), t2.toString());
                        BaseApplication.get().getSocketObservables().emitTransactionConfirmation(t);
                        BaseApplication.get().getSocketObservables().emitTransactionSent(transactionSent.body());
                        String parsedInput = activity.getBinding().amount.getText().toString().replace(",", ".");

                        final Intent intent = new Intent();
                        intent.putExtra(INTENT_WALLET_ADDRESS, activity.getBinding().walletAddress.getText().toString());
                        intent.putExtra(INTENT_WITHDRAW_AMOUNT, new BigDecimal(parsedInput));
                        activity.setResult(RESULT_OK, intent);
                        activity.finish();
                        activity.overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
                    }
                };
            }
        };
    }

    private boolean validate() {
        try {
            final NumberFormat nf = NumberFormat.getInstance(LocaleUtil.getLocale());
            final String inputtedText = this.activity.getBinding().amount.getText().toString();
            String parsedInput = inputtedText.replace(".", ",");

            final BigDecimal amountRequested = new BigDecimal(nf.parse(parsedInput).toString());
            final String toAddress = this.activity.getBinding().walletAddress.getText().toString();
            this.activity.getBinding().walletAddress.setText(toAddress.replaceFirst("ethereum:", ""));

            Log.d(TAG, "validate: " + toAddress);

            Log.d(TAG, "validate: " + currentBalance + " " + parsedInput);

            if (amountRequested.compareTo(minWithdrawLimit) > 0 && amountRequested.compareTo(this.currentBalance) <= 0) {
                return true;
            }
        } catch (final NumberFormatException | ParseException ex) {
            LogUtil.e(getClass(), ex.toString());
        }

        String errorMessage = this.activity.getResources().getString(R.string.withdraw__amount_error);
        SnackbarUtil.make(this.activity.getBinding().root, errorMessage).show();

        this.activity.getBinding().amount.requestFocus();
        return false;
    }

    private boolean userHasEnoughReputationScore() {
        // Todo: Reputation required for withdrawal should be dictated by the server
        if (currentUser == null || currentUser.getReputationScore() == 0) {
            return false;
        }
        return true;
    }

    private void registerObservables() {
        BaseApplication.get().getLocalBalanceManager().getObservable().subscribe(this.newBalanceSubscriber);
        BaseApplication.get().getUserManager().getObservable().subscribe(this.userSubscriber);
        BaseApplication.get().getLocalBalanceManager().getReputationObservable().subscribe(this.newReputationSubscriber);
    }

    private final OnNextObserver<User> userSubscriber = new OnNextObserver<User>() {
        @Override
        public void onNext(final User user) {
            currentUser = user;
            refreshButtonStates();
            this.onCompleted();
        }
    };

    private void refreshButtonStates() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (activity == null) {
                    return;
                }
                updateSendButtonEnabledState();
            }
        });
    }
}
