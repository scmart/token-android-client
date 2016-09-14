package com.bakkenbaeck.toshi.model;


import com.bakkenbaeck.toshi.network.ws.model.Payment;
import com.bakkenbaeck.toshi.util.OnNextObserver;
import com.bakkenbaeck.toshi.view.BaseApplication;

import java.math.BigInteger;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class OfflineBalance {

    private final BehaviorSubject<BigInteger> balanceSubject = BehaviorSubject.create();
    private BigInteger amountInWei;
    private boolean hasWithdrawn = false;
    private int numberOfRewards = -1;

    public OfflineBalance() {
        this.amountInWei = BigInteger.ZERO;
        initBalanceListeners();
    }

    private void initBalanceListeners() {
        BaseApplication.get().getUserManager().getObservable().subscribe(this.currentUserSubscriber);
        BaseApplication.get().getSocketObservables().getPaymentObservable().subscribe(this.newPaymentSubscriber);
    }

    private final OnNextObserver<User> currentUserSubscriber = new OnNextObserver<User>() {
        @Override
        public void onNext(final User user) {
            setBalance(user.getBalance());
        }
    };

    private final OnNextObserver<Payment> newPaymentSubscriber = new OnNextObserver<Payment>() {
        @Override
        public void onNext(final Payment payment) {
            setBalance(payment.getNewBalance());
        }
    };

    public Observable<BigInteger> getObservable() {
        return this.balanceSubject.asObservable();
    }

    private void setBalance(final BigInteger balance) {
        ++numberOfRewards;
        this.amountInWei = balance;
        emitNewBalance();
    }

    public void subtract(final BigInteger amount) {
        this.amountInWei = this.amountInWei.subtract(amount);
        this.hasWithdrawn = true;
    }

    public boolean hasWithdraw() {
        return this.hasWithdrawn;
    }

    public int getNumberOfRewards() {
        return this.numberOfRewards;
    }

    private void emitNewBalance() {
        this.balanceSubject.onNext(this.amountInWei);
    }

    @Override
    public String toString() {
        if (this.amountInWei == null) {
            return "0";
        } else {
            return this.amountInWei.toString();
        }
    }
}