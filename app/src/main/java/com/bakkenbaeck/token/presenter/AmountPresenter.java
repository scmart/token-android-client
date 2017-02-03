package com.bakkenbaeck.token.presenter;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.util.TypeConverter;
import com.bakkenbaeck.token.util.EthUtil;
import com.bakkenbaeck.token.util.LocaleUtil;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.AmountActivity;
import com.bakkenbaeck.token.view.adapter.AmountInputAdapter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormatSymbols;

public class AmountPresenter implements Presenter<AmountActivity> {

    public static final String INTENT_EXTRA__ETH_AMOUNT = "eth_amount";
    private AmountActivity activity;
    private char separator;
    private char zero;
    private String encodedEthAmount;

    @Override
    public void onViewAttached(AmountActivity view) {
        this.activity = view;
        initView();
        initToolbar();
        initSeparator();
    }

    private void initSeparator() {
        final DecimalFormatSymbols dcf = LocaleUtil.getDecimalFormatSymbols();
        this.separator = dcf.getMonetaryDecimalSeparator();
        this.zero = dcf.getZeroDigit();
    }

    private void initView() {
        this.activity.getBinding().amountInputView.setOnAmountClickedListener(this.amountClickedListener);
        this.activity.getBinding().btnContinue.setOnClickListener(this.continueClickListener);
        updateEthAmount();
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

    private void initToolbar() {
        this.activity.getBinding().title.setText(this.activity.getString(R.string.send));
        this.activity.getBinding().closeButton.setOnClickListener(this.backButtonClickListener);
    }

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
        this.activity.getBinding().localValue.setText(newLocalValue);
        updateEthAmount();
    }

    private void handleValueClicked(final char value) {
        if (value == separator) {
            handleSeparatorClicked();
        } else {
            updateValue(value);
        }
    }

    private void handleSeparatorClicked() {
        final String currentLocalValue = this.activity.getBinding().localValue.getText().toString();

        // Only allow a single decimal separator
        if (currentLocalValue.indexOf(separator) >= 0) {
            return;
        }

        // If a separator is the first character; append a zero
        if (currentLocalValue.length() == 0) {
            updateValue(zero);
        }

        updateValue(separator);
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

        if (currentLocalValue.length() == 0 && value == zero) {
            return;
        }

        final String newLocalValue = currentLocalValue + value;
        this.activity.getBinding().localValue.setText(newLocalValue);
    }

    private void updateEthAmount() {
        final BigDecimal localValue = getLocalValueAsBigDecimal();

        final BigDecimal ethAmount = BaseApplication
                .get()
                .getTokenManager()
                .getBalanceManager()
                .convertLocalCurrencyToEth(localValue);

        this.activity.getBinding().ethValue.setText(ethAmount.toPlainString());
        this.activity.getBinding().btnContinue.setEnabled(ethAmount.compareTo(BigDecimal.ZERO) != 0);

        final BigInteger weiAmount = EthUtil.ethToWei(ethAmount);
        this.encodedEthAmount = TypeConverter.toJsonHex(weiAmount);
    }

    @NonNull
    private BigDecimal getLocalValueAsBigDecimal() {
        final String currentLocalValue = this.activity.getBinding().localValue.getText().toString();
        if (currentLocalValue.length() == 0) {
            return BigDecimal.ZERO;
        }

        final String[] parts = currentLocalValue.split(String.valueOf(separator));
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
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        this.activity = null;
    }
}
