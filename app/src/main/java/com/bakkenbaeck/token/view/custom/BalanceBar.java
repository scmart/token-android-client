package com.bakkenbaeck.token.view.custom;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.LocalBalance;

import java.math.BigDecimal;
import java.util.Locale;

public class BalanceBar extends LinearLayout {

    public interface OnLevelClicked{
        void onClickListener();
    }

    private OnLevelClicked clickListener;

    public void setOnLevelClicked(OnLevelClicked listener){
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
    }

    public void disableClickEvents(boolean disable){
        if(!disable){
            findViewById(R.id.textView).setOnClickListener(null);

            findViewById(R.id.level).setOnClickListener(null);
        }else{
            findViewById(R.id.textView).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickListener != null){
                        clickListener.onClickListener();
                    }
                }
            });

            findViewById(R.id.level).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(clickListener != null){
                        clickListener.onClickListener();
                    }
                }
            });
        }
    }

    public void setBalance(final LocalBalance balance) {
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                final String newBalance = balance.getUnconfirmedBalanceAsEth().setScale(4, BigDecimal.ROUND_DOWN).toString();
                ((TextView) findViewById(R.id.balance)).setText(newBalance);
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
        final String substring = String.format(Locale.getDefault(), "%.4f", usd);
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.eth_value)).setText(substring);
            }
        }, 200);
    }
}
