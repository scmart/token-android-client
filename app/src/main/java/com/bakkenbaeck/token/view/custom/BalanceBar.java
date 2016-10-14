package com.bakkenbaeck.token.view.custom;


import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bakkenbaeck.token.R;

import rx.Observable;
import rx.subjects.PublishSubject;

public class BalanceBar extends LinearLayout {
    private static final String TAG = "BalanceBar";

    private PublishSubject<Boolean> levelClickSubject = PublishSubject.create();

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

        findViewById(R.id.level).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(levelClickSubject != null) {
                    levelClickSubject.onNext(true);
                }
            }
        });
    }

    public void setBalance(final String balance) {
        this.postDelayed(new Runnable() {
            @Override
            public void run() {
                String shortBalance = balance.substring(0,6);
                ((TextView)findViewById(R.id.balance)).setText(shortBalance);
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

    public Observable<Boolean> getLevelClickObservable(){
        return levelClickSubject.asObservable();
    }

}
