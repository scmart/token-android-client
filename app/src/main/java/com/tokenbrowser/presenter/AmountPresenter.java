package com.tokenbrowser.presenter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import com.tokenbrowser.token.R;
import com.tokenbrowser.crypto.util.TypeConverter;
import com.tokenbrowser.util.EthUtil;
import com.tokenbrowser.util.LocaleUtil;
import com.tokenbrowser.util.PaymentType;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.AmountActivity;
import com.tokenbrowser.view.adapter.AmountInputAdapter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormatSymbols;

import rx.subscriptions.CompositeSubscription;

public class AmountPresenter implements Presenter<AmountActivity> {

    public static final String INTENT_EXTRA__ETH_AMOUNT = "eth_amount";

    private AmountActivity activity;
    private char separator;
    private char zero;
    private String encodedEthAmount;
    private @PaymentType.Type  int viewType;
    private CompositeSubscription subscriptions;

    @Override
    public void onViewAttached(AmountActivity view) {
        this.activity = view;

        if (this.subscriptions == null) {
            this.subscriptions = new CompositeSubscription();
        }

        getIntentData();
        initView();
        initSeparator();
    }

    @SuppressWarnings("WrongConstant")
    private void getIntentData() {
        this.viewType = this.activity.getIntent().getIntExtra(AmountActivity.VIEW_TYPE, PaymentType.TYPE_SEND);
    }

    private void initView() {
        initToolbar();
        updateEthAmount();

        this.activity.getBinding().amountInputView.setOnAmountClickedListener(this.amountClickedListener);
        this.activity.getBinding().btnContinue.setOnClickListener(this.continueClickListener);
    }

    private void initSeparator() {
        final DecimalFormatSymbols dcf = LocaleUtil.getDecimalFormatSymbols();
        this.separator = dcf.getMonetaryDecimalSeparator();
        this.zero = dcf.getZeroDigit();
    }

    private void initToolbar() {
        final String title = this.viewType == PaymentType.TYPE_SEND
                ? this.activity.getString(R.string.send)
                : this.activity.getString(R.string.request);

        this.activity.getBinding().title.setText(title);
        this.activity.getBinding().closeButton.setOnClickListener(this.backButtonClickListener);
    }

    private View.OnClickListener continueClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View unused) {
            if (encodedEthAmount == null) {
                return;
            }

            final Intent intent = new Intent();
            intent.putExtra(INTENT_EXTRA__ETH_AMOUNT, encodedEthAmount);
            activity.setResult(Activity.RESULT_OK, intent);
            activity.finish();
        }
    };

    private View.OnClickListener backButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            activity.finish();
        }
    };

    private AmountInputAdapter.OnKeyboardItemClicked amountClickedListener = new AmountInputAdapter.OnKeyboardItemClicked() {
        @Override
        public void onValueClicked(final char value) {
            handleValueClicked(value);
        }

        @Override
        public void onBackSpaceClicked() {
            handleBackspaceClicked();
        }
    };

    private void handleBackspaceClicked() {
        final String currentLocalValue = this.activity.getBinding().localValue.getText().toString();
        final int endIndex = Math.max(0, currentLocalValue.length() -1);
        final String newLocalValue = currentLocalValue.substring(0, endIndex);

        if (newLocalValue.equals(String.valueOf(zero))) {
            this.activity.getBinding().localValue.setText("");
        } else {
            this.activity.getBinding().localValue.setText(newLocalValue);
        }
        updateEthAmount();
    }

    private void handleValueClicked(final char value) {
        if (value == this.separator) {
            handleSeparatorClicked();
        } else {
            updateValue(value);
        }
    }

    private void handleSeparatorClicked() {
        final String currentLocalValue = this.activity.getBinding().localValue.getText().toString();

        // Only allow a single decimal separator
        if (currentLocalValue.indexOf(this.separator) >= 0) {
            return;
        }

        updateValue(this.separator);
    }

    private void updateValue(final char value) {
        appendValueInUi(value);
        updateEthAmount();
    }

    private void appendValueInUi(final char value) {
        final String currentLocalValue = this.activity.getBinding().localValue.getText().toString();
        if (currentLocalValue.length() >= 10) {
            return;
        }

        if (currentLocalValue.length() == 0 && value == this.zero) {
            return;
        }

        if (currentLocalValue.length() == 0 && value == this.separator) {
            final String localValue = String.format("%s%s", String.valueOf(this.zero), String.valueOf(this.separator));
            this.activity.getBinding().localValue.setText(localValue);
            return;
        }

        final String newLocalValue = currentLocalValue + value;
        this.activity.getBinding().localValue.setText(newLocalValue);
    }

    private void updateEthAmount() {
        final BigDecimal localValue = getLocalValueAsBigDecimal();

        this.subscriptions.add(
                BaseApplication
                .get()
                .getTokenManager()
                .getBalanceManager()
                .convertLocalCurrencyToEth(localValue)
                .subscribe((ethAmount) -> {
                    this.activity.getBinding().ethValue.setText(ethAmount.toPlainString());
                    this.activity.getBinding().btnContinue.setEnabled(ethAmount.compareTo(BigDecimal.ZERO) != 0);

                    final BigInteger weiAmount = EthUtil.ethToWei(ethAmount);
                    this.encodedEthAmount = TypeConverter.toJsonHex(weiAmount);
                })
        );
    }

    @NonNull
    private BigDecimal getLocalValueAsBigDecimal() {
        final String currentLocalValue = this.activity.getBinding().localValue.getText().toString();
        if (currentLocalValue.length() == 0 || currentLocalValue.equals(String.valueOf(this.separator))) {
            return BigDecimal.ZERO;
        }

        final String[] parts = currentLocalValue.split(String.valueOf(this.separator));
        final String integerPart = parts.length == 0 ? currentLocalValue : parts[0];
        final String fractionalPart = parts.length < 2 ? "0" : parts[1];
        final String fullValue = integerPart + "." + fractionalPart;

        final String trimmedValue = fullValue.endsWith(".0")
                ? fullValue.substring(0, fullValue.length() - 2)
                : fullValue;

        return new BigDecimal(trimmedValue);
    }

    @Override
    public void onViewDetached() {
        this.subscriptions.clear();
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.subscriptions = null;
    }
}