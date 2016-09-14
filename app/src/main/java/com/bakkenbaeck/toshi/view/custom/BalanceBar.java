package com.bakkenbaeck.toshi.view.custom;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.bakkenbaeck.toshi.R;
import com.robinhood.ticker.TickerUtils;
import com.robinhood.ticker.TickerView;

public class BalanceBar extends LinearLayout {

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
        ((TickerView)findViewById(R.id.balance)).setCharacterList(TickerUtils.getDefaultListForUSCurrency());
    }

    public void setBalance(final String balance) {
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.dot_loader).setVisibility(INVISIBLE);
                ((TickerView) findViewById(R.id.balance)).setText(balance);
            }
        }, 200);
    }
}
