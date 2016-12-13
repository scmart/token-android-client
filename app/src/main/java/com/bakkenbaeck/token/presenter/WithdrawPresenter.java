package com.bakkenbaeck.token.presenter;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.model.ActivityResultHolder;
import com.bakkenbaeck.token.network.rest.BalanceService;
import com.bakkenbaeck.token.network.rest.model.SentTransaction;
import com.bakkenbaeck.token.network.rest.model.SignedTransaction;
import com.bakkenbaeck.token.network.rest.model.TransactionRequest;
import com.bakkenbaeck.token.network.rest.model.UnsignedTransaction;
import com.bakkenbaeck.token.util.EthUtil;
import com.bakkenbaeck.token.util.LocaleUtil;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.Fragment.QrFragment;
import com.bakkenbaeck.token.view.activity.BarcodeScannerActivity;
import com.bakkenbaeck.token.view.activity.WithdrawActivity;
import com.bakkenbaeck.token.view.adapter.PreviousWalletAddress;
import com.bakkenbaeck.token.view.dialog.ProgressDialog;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParseException;

import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

public class WithdrawPresenter implements Presenter<WithdrawActivity>, QrFragment.OnFragmentClosed {

    private WithdrawActivity activity;
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
            initProgressDialog();
        }
    }

    private void initProgressDialog() {
        progressDialog = ProgressDialog.newInstance();
    }

    private void initButtons() {
        this.activity.getBinding().barcodeButton.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(final View v) {
                showBarcodeActivity();
            }
        });

        reEnableDialogListeners();

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
    }

    private void updateSendButtonEnabledState() {
        final Editable walletAddress = this.activity.getBinding().walletAddress.getText();
        final String amount = this.activity.getBinding().amount.getText().toString();
        final boolean shouldEnableButton = walletAddress.length() > 0 && amount.length() > 0;
        enableSendButton(shouldEnableButton);
        activity.getBinding().sendButton.setEnabled(shouldEnableButton);
    }

    private void enableSendButton(boolean enabled) {
        if (enabled) {
            activity.getBinding().sendButton.setTextColor(Color.parseColor("#FFFFFF"));
            activity.getBinding().sendButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.btn_with_radius));
        } else {
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

    @Override
    public void onViewDetached() {
        String walletAddress = this.activity.getBinding().walletAddress.getText().toString();
        previousWalletAddress.setAddress(walletAddress);
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
        if(result == null || result.getContents() == null || result.getContents().length() <= 0) {
            return;
        }

        this.activity.getBinding().walletAddress.setText(result.getContents().replaceFirst("ethereum:", ""));
    }

    private void handleSendClicked() {
        if (!validate()) {
            return;
        }

        BaseApplication.get()
                .getTokenManager().getWallet()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSuccessSubscriber<HDWallet>() {
                    @Override
                    public void onSuccess(final HDWallet wallet) {
                        sendTransaction(wallet);
                        this.unsubscribe();
                    }
                });
    }

    private BigInteger parseInputtedAmount() throws ParseException {
        final DecimalFormat nf = (DecimalFormat) DecimalFormat.getInstance(LocaleUtil.getLocale());
        nf.setParseBigDecimal(true);
        final String inputtedText = this.activity.getBinding().amount.getText().toString();

        final BigDecimal amountInEth = (BigDecimal) nf.parse(inputtedText);
        return EthUtil.ethToWei(amountInEth);
    }

    private void sendTransaction(final HDWallet wallet) {

        final BigInteger amountInWei;
        try {
            amountInWei = parseInputtedAmount();
        } catch (final ParseException ex) {
            LogUtil.e(getClass(), ex.toString());
            return;
        }

        final String toAddress = this.activity.getBinding().walletAddress.getText().toString();
        final TransactionRequest transactionRequest = new TransactionRequest()
                .setAmount(amountInWei)
                .setFromAddress(wallet.getAddress())
                .setToAddress(toAddress);

        BalanceService.getApi()
                .createTransaction(transactionRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleSuccessSubscriber<UnsignedTransaction>() {
                    @Override
                    public void onSuccess(final UnsignedTransaction unsignedTransaction) {
                        signAndSendTransaction(unsignedTransaction, wallet);
                        this.unsubscribe();
                    }
                });

        progressDialog.show(this.activity.getSupportFragmentManager(), "progressDialog");
        this.previousWalletAddress.setAddress(toAddress);

    }

    private void signAndSendTransaction(final UnsignedTransaction unsignedTransaction, final HDWallet wallet) {

        final String transaction = unsignedTransaction.getTransaction();
        final String signature = wallet.signString(transaction);

        final SignedTransaction signedTransaction = new SignedTransaction()
                .setEncodedTransaction(transaction)
                .setSignature(signature);

        BalanceService.getApi()
                .sendSignedTransaction(signedTransaction)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new SingleSubscriber<SentTransaction>() {
                    @Override
                    public void onSuccess(final SentTransaction value) {
                        showBalanceError("It worked!");
                        this.unsubscribe();
                    }

                    @Override
                    public void onError(final Throwable error) {
                        LogUtil.e(getClass(), error.getMessage());
                        showBalanceError(error.getMessage());
                        this.unsubscribe();
                    }
                });
    }

    private void showAddressError(final String errorMessage) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final EditText address = activity.getBinding().walletAddress;
                address.requestFocus();
                address.setError(errorMessage, null);
            }
        });
    }

    private void showBalanceError(final String errorMessage) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                final EditText amount = activity.getBinding().amount;
                amount.requestFocus();
                amount.setError(errorMessage, null);
            }
        });
    }

    private boolean validate() {
        BigDecimal amountRequested = BigDecimal.ZERO;

        try {
            final DecimalFormat nf = (DecimalFormat) DecimalFormat.getInstance(LocaleUtil.getLocale());
            nf.setParseBigDecimal(true);
            final String inputtedText = this.activity.getBinding().amount.getText().toString();
            amountRequested = (BigDecimal) nf.parse(inputtedText);

            if (amountRequested.compareTo(minWithdrawLimit) > 0 && amountRequested.compareTo(this.currentBalance) <= 0) {
                return true;
            }
        } catch (final NumberFormatException | ParseException ex) {
            LogUtil.e(getClass(), ex.toString());
            showBalanceError(this.activity.getResources().getString(R.string.withdraw__amount_error));
        }

        if (this.currentBalance.compareTo(BigDecimal.ZERO) == 0) {
            showBalanceError(this.activity.getResources().getString(R.string.withdraw__amount_error_zero));
        } else if(amountRequested != null && amountRequested.compareTo(currentBalance) == 1) {
            showBalanceError(this.activity.getResources().getString(R.string.withdraw__amount_error_bigger));
        } else {
            showBalanceError(this.activity.getResources().getString(R.string.withdraw__amount_error));
        }

        return false;
    }

    private void showQrFragment() {
        FragmentManager fm = this.activity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.enter_fade_in, R.anim.exit_fade_out);
        QrFragment qrFragment = QrFragment.newInstance();
        ft.add(R.id.fragmentRoot, qrFragment, QrFragment.TAG).addToBackStack(QrFragment.TAG).commit();
        qrFragment.setOnFragmentClosed(this);
    }

    public void removeQrFragment() {
        FragmentManager fm = this.activity.getSupportFragmentManager();
        Fragment qrFragment = fm.findFragmentByTag(QrFragment.TAG);

        if(qrFragment != null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(R.anim.enter_fade_in, R.anim.exit_fade_out);
            ft.remove(qrFragment).commit();
            fm.popBackStackImmediate(QrFragment.TAG,  FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    private void reEnableDialogListeners() {
        QrFragment durationDialog = (QrFragment) this.activity.getSupportFragmentManager().findFragmentByTag(QrFragment.TAG);
        if(durationDialog != null) {
            durationDialog.setOnFragmentClosed(this);
        }
    }

    @Override
    public void onClose() {
        removeQrFragment();
    }
}
