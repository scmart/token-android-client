package com.bakkenbaeck.toshi.presenter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bakkenbaeck.toshi.R;
import com.bakkenbaeck.toshi.model.ActivityResultHolder;
import com.bakkenbaeck.toshi.model.LocalBalance;
import com.bakkenbaeck.toshi.model.User;
import com.bakkenbaeck.toshi.network.rest.ToshiService;
import com.bakkenbaeck.toshi.network.rest.model.SignatureRequest;
import com.bakkenbaeck.toshi.network.rest.model.SignedWithdrawalRequest;
import com.bakkenbaeck.toshi.network.rest.model.TransactionSent;
import com.bakkenbaeck.toshi.network.rest.model.WithdrawalRequest;
import com.bakkenbaeck.toshi.util.EthUtil;
import com.bakkenbaeck.toshi.util.LocaleUtil;
import com.bakkenbaeck.toshi.util.LogUtil;
import com.bakkenbaeck.toshi.util.OnNextObserver;
import com.bakkenbaeck.toshi.util.RetryWithBackoff;
import com.bakkenbaeck.toshi.view.BaseApplication;
import com.bakkenbaeck.toshi.view.activity.BarcodeScannerActivity;
import com.bakkenbaeck.toshi.view.activity.WithdrawActivity;
import com.bakkenbaeck.toshi.view.adapter.PreviousWalletAddress;
import com.bakkenbaeck.toshi.view.dialog.PhoneInputDialog;
import com.bakkenbaeck.toshi.view.dialog.VerificationCodeDialog;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;

import rx.Subscriber;

import static android.app.Activity.RESULT_OK;

public class WithdrawPresenter implements Presenter<WithdrawActivity> {

    static final String INTENT_WALLET_ADDRESS = "wallet_address";
    static final String INTENT_WITHDRAW_AMOUNT = "withdraw_amount";

    private WithdrawActivity activity;
    private User currentUser;
    private boolean firstTimeAttaching = true;
    private BigDecimal currentBalance = BigDecimal.ZERO;
    private final BigDecimal minWithdrawLimit = new BigDecimal("0.0000000001");
    private ProgressDialog progressDialog;

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
            initProgressDialog();
        }
    }

    private void initProgressDialog() {
        this.progressDialog = new ProgressDialog(this.activity, R.style.DialogTheme);
        this.progressDialog.setTitle("Withdrawing...");
        this.progressDialog.setMessage("It may take a few minutes before the ether appears in the receiver wallet");
        this.progressDialog.setIndeterminate(true);
    }

    private void initButtons() {
        this.activity.getBinding().barcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                showBarcodeActivity();
            }
        });

        this.activity.getBinding().sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                handleSendClicked();
            }
        });

        this.activity.getBinding().walletAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {}

            @Override
            public void onTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {
                final boolean showFab = charSequence.length() > 0;
                if (showFab) {
                    activity.getBinding().sendButton.animate().alpha(1.0f).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(final Animator animation) {
                            super.onAnimationStart(animation);
                            activity.getBinding().sendButton.setVisibility(View.VISIBLE);
                        }
                    });
                } else {
                    activity.getBinding().sendButton.animate().alpha(0.0f).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(final Animator animation) {
                            super.onAnimationEnd(animation);
                            activity.getBinding().sendButton.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            }

            @Override
            public void afterTextChanged(final Editable editable) {}
        });
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
                tryPopulateAmountField(currentBalance, newBalance.confirmedBalanceStringMinusTransferFee());
                currentBalance = newBalance.getConfirmedBalanceAsEthMinusTransferFee();
            }
        }
    };

    private void tryPopulateAmountField(final BigDecimal previousBalance, final String newBalanceAsEthString) {
        try {
            if (new BigDecimal(this.activity.getBinding().amount.getText().toString()).equals(previousBalance)) {
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

    public void onPhoneInputSuccess(final PhoneInputDialog dialog) {
        final String phoneNumber = dialog.getInputtedPhoneNumber();
        final VerificationCodeDialog vcDialog = VerificationCodeDialog.newInstance(phoneNumber);
        vcDialog.show(this.activity.getSupportFragmentManager(), "dialog");
    }

    public void onVerificationSuccess() {
        Snackbar.make(
                this.activity.getBinding().getRoot(),
                this.activity.getString(R.string.verification_success),
                Snackbar.LENGTH_INDEFINITE);
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

        final BigDecimal amountInEth = new BigDecimal(this.activity.getBinding().amount.getText().toString());
        final BigInteger amountInWei = EthUtil.ethToWei(amountInEth);
        final String toAddress = this.activity.getBinding().walletAddress.getText().toString();
        final WithdrawalRequest withdrawalRequest = new WithdrawalRequest(amountInWei, toAddress);
        ToshiService.getApi()
                .postWithdrawalRequest(this.currentUser.getAuthToken(), withdrawalRequest)
                .retryWhen(new RetryWithBackoff(5))
                .subscribe(generateSigningSubscriber());
        this.progressDialog.show();
        this.previousWalletAddress.setAddress(toAddress);
    }

    private Subscriber<SignatureRequest> generateSigningSubscriber() {
        return new Subscriber<SignatureRequest>() {
            @Override
            public void onCompleted() {}

            @Override
            public void onError(final Throwable ex) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity, "There was a problem withdrawing, please try again.", Toast.LENGTH_LONG).show();
                        progressDialog.dismiss();
                    }
                });
                LogUtil.e(getClass(), "postWithdrawalRequest: " + ex);
            }

            @Override
            public void onNext(final SignatureRequest signatureRequest) {
                final String unsignedTransaction = signatureRequest.getTransaction();
                final String signature = BaseApplication.get().getUserManager().signTransaction(unsignedTransaction);
                final SignedWithdrawalRequest request = new SignedWithdrawalRequest(unsignedTransaction, signature);
                ToshiService.getApi()
                        .postSignedWithdrawal(currentUser.getAuthToken(), request)
                        .retryWhen(new RetryWithBackoff(5))
                        .subscribe(generateSignedWithdrawalSubscriber());
            }

            private Subscriber<TransactionSent> generateSignedWithdrawalSubscriber() {
                return new Subscriber<TransactionSent>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(final Throwable ex) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, R.string.error__withdrawing, Toast.LENGTH_LONG).show();
                                progressDialog.dismiss();
                            }
                        });
                        LogUtil.e(getClass(), "postSignedWithdrawal: " + ex);
                    }

                    @Override
                    public void onNext(final TransactionSent transactionSent) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                            }
                        });

                        final Intent intent = new Intent();
                        intent.putExtra(INTENT_WALLET_ADDRESS, activity.getBinding().walletAddress.getText().toString());
                        intent.putExtra(INTENT_WITHDRAW_AMOUNT, new BigDecimal(activity.getBinding().amount.getText().toString()));
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
            final BigDecimal amountRequested = new BigDecimal(nf.parse(inputtedText).toString());

            if (amountRequested.compareTo(this.minWithdrawLimit) > 0 && amountRequested.compareTo(this.currentBalance) <= 0) {
                return userHasEnoughReputationScore();
            }
        } catch (final NumberFormatException | ParseException ex) {
            LogUtil.e(getClass(), ex.toString());
        }

        this.activity.getBinding().amount.setError(this.activity.getResources().getString(R.string.withdraw__amount_error));
        this.activity.getBinding().amount.requestFocus();
        return false;
    }

    private boolean userHasEnoughReputationScore() {
        // Todo: Reputation required for withdrawal should be dictated by the server
        if (currentUser == null || currentUser.getReputationScore() == 0) {
            new PhoneInputDialog().show(this.activity.getSupportFragmentManager(), "dialog");
            return false;
        }
        return true;
    }

    private void registerObservables() {
        BaseApplication.get().getLocalBalanceManager().getObservable().subscribe(this.newBalanceSubscriber);
        BaseApplication.get().getUserManager().getObservable().subscribe(this.userSubscriber);
    }

    private final OnNextObserver<User> userSubscriber = new OnNextObserver<User>() {
        @Override
        public void onNext(final User user) {
            currentUser = user;
            this.onCompleted();
        }
    };
}
