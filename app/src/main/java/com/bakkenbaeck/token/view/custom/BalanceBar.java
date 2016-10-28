package com.bakkenbaeck.token.view.custom;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.LocalBalance;
import com.bakkenbaeck.token.util.LocaleUtil;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class BalanceBar extends LinearLayout {

    private OnClickListener clickListener;

    public void setOnLevelClicked(final OnClickListener listener){
        clickListener = listener;
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

    public void disableClickEvents() {
        findViewById(R.id.textView).setOnClickListener(null);
        findViewById(R.id.level).setOnClickListener(null);
    }

    public void enableClickEvents() {
        findViewById(R.id.textView).setOnClickListener(this.clickListener);
        findViewById(R.id.level).setOnClickListener(this.clickListener);
    }

    public void setBalance(final LocalBalance balance) {
        final BigDecimal unconfirmedBalance = balance.getUnconfirmedBalanceAsEth();
        setBalanceFromBigDecimal(unconfirmedBalance);
    }

    private void setBalanceFromBigDecimal(final BigDecimal unconfirmedBalance) {
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                final NumberFormat formatter = NumberFormat.getNumberInstance(LocaleUtil.getLocale());
                final String substring = String.format(LocaleUtil.getLocale(), "%.4f", unconfirmedBalance.setScale(4, BigDecimal.ROUND_DOWN));
                ((TextView) findViewById(R.id.balance)).setText(substring);
            }
        }, 200);
    }

    public void setReputation(final int level){
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.level)).setText(String.valueOf(level));
            }
        }, 200);
    }

    public void setEthValue(final double ethValue, final BigDecimal unconfirmedBalance){
        final double usd = ethValue * unconfirmedBalance.doubleValue();
        setEthValueFromDouble(usd);
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
