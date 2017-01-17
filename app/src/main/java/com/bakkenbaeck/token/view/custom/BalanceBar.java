package com.bakkenbaeck.token.view.custom;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.util.LocaleUtil;

import java.math.BigDecimal;

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
    }

    private void setEmptyValues() {
        setBalanceFromBigDecimal(BigDecimal.ZERO);
        setEthValueFromDouble(0);
    }

    private void setBalanceFromBigDecimal(final BigDecimal unconfirmedBalance) {
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                final String substring = String.format(LocaleUtil.getLocale(), "%.4f", unconfirmedBalance.setScale(4, BigDecimal.ROUND_DOWN));
                ((TextView) findViewById(R.id.balance)).setText(substring);
            }
        }, 200);
    }

    private void setEthValueFromDouble(final double usd) {
        final String substring = String.format(LocaleUtil.getLocale(), "%.2f", usd);
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.eth_value)).setText(substring);
            }
        }, 200);
    }
}
