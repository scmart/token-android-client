/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.view.custom;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tokenbrowser.model.network.Balance;
import com.tokenbrowser.R;
import com.tokenbrowser.util.EthUtil;
import com.tokenbrowser.util.SoundManager;
import com.tokenbrowser.view.BaseApplication;

import java.math.BigInteger;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class BalanceBar extends LinearLayout {

    private BigInteger previousWeiBalance;
    private CompositeSubscription subscriptions;

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
        showBalanceInUi(BigInteger.ZERO);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.subscriptions = new CompositeSubscription();
        attachBalanceSubscriber();
    }

    private void attachBalanceSubscriber() {
        final Subscription getBalanceSub =
                BaseApplication
                .get()
                .getTokenManager()
                .getBalanceManager()
                .getBalanceObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleNewBalance);
        this.subscriptions.add(getBalanceSub);
    }

    private void handleNewBalance(final Balance balance) {
        setEthBalanceFromBigInteger(balance.getUnconfirmedBalance());
        final Subscription getLocalBalanceSub =
                balance
                .getFormattedLocalBalance()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setLocalBalance);
        this.subscriptions.add(getLocalBalanceSub);
    }

    private void setEthBalanceFromBigInteger(final BigInteger weiBalance) {
        showBalanceInUi(weiBalance);
        tryPlaySound(weiBalance);
    }

    private void tryPlaySound(final BigInteger weiBalance) {
        if (this.previousWeiBalance == null) {
            this.previousWeiBalance = weiBalance;
            return;
        }

        if (this.previousWeiBalance.compareTo(weiBalance) == 0) {
            return;
        }

        this.previousWeiBalance = weiBalance;
        SoundManager.getInstance().playSound(SoundManager.BALANCE_CHANGE);
    }

    private void showBalanceInUi(final BigInteger weiBalance) {
        final String stringBalance = EthUtil.weiAmountToUserVisibleString(weiBalance);
        ((TextView) findViewById(R.id.eth_balance)).setText(stringBalance);
    }

    private void setLocalBalance(final String localBalance) {
        ((TextView) findViewById(R.id.local_currency_balance)).setText(localBalance);
    }

    @Override
    public void onDetachedFromWindow() {
        this.subscriptions.clear();
        this.subscriptions = null;
        super.onDetachedFromWindow();
    }

}
