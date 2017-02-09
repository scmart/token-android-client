package com.bakkenbaeck.token.view.custom;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.network.Balance;
import com.bakkenbaeck.token.util.EthUtil;
import com.bakkenbaeck.token.util.LocaleUtil;
import com.bakkenbaeck.token.view.BaseApplication;

import java.math.BigDecimal;
import java.math.BigInteger;

import rx.android.schedulers.AndroidSchedulers;

public class BalanceBar extends LinearLayout {

    public void setOnBalanceClicked(final OnClickListener listener) {
        findViewById(R.id.balanceWrapper).setOnClickListener(listener);
    }

    public void setOnPayClicked(final OnClickListener listener) {
        findViewById(R.id.pay_button).setOnClickListener(listener);
        findViewById(R.id.pay_button).setVisibility(VISIBLE);
    }

    public void setOnRequestClicked(final OnClickListener listener) {
        findViewById(R.id.request_button).setOnClickListener(listener);
        findViewById(R.id.request_button).setVisibility(VISIBLE);
    }

    public BalanceBar(final Context context) {
        super(context);
        init();
    }

    public BalanceBar(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view__balance_bar, this);
        setEmptyValues();
        setBalance();
    }

    private void setEmptyValues() {
        setBalanceFromBigInteger(BigInteger.ZERO);
        setEthValueFromDouble(0);
    }

    private void setBalanceFromBigInteger(final BigInteger weiBalance) {
        final BigDecimal ethBalance = EthUtil.weiToEth(weiBalance);
        final String substring = String.format(LocaleUtil.getLocale(), "%.4f", ethBalance.setScale(4, BigDecimal.ROUND_DOWN));
        ((TextView) findViewById(R.id.balance)).setText(substring);
    }

    private void setEthValueFromDouble(final double usd) {
        final String substring = String.format(LocaleUtil.getLocale(), "%.2f", usd);
        ((TextView)findViewById(R.id.eth_value)).setText(substring);
    }

    private void setBalance() {
        BaseApplication
            .get()
            .getTokenManager()
            .getBalanceManager()
            .getBalanceObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::handleNewBalance);
    }

    private void handleNewBalance(final Balance balance) {
        setBalanceFromBigInteger(balance.getConfirmedBalance());
    }
}
