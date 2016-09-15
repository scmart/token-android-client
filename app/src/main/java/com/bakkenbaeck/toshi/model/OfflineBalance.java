package com.bakkenbaeck.toshi.model;


import com.bakkenbaeck.toshi.network.ws.model.Payment;
import com.bakkenbaeck.toshi.util.OnNextObserver;
import com.bakkenbaeck.toshi.view.BaseApplication;

import java.math.BigInteger;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class OfflineBalance {

    private final BehaviorSubject<BigInteger> balanceSubject = BehaviorSubject.create();
    private final PublishSubject<Void> upsellSubject = PublishSubject.create();

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

    public Observable<Void> getUpsellObservable() {
        return this.upsellSubject.asObservable();
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


    // True if the wallet is in a state wher we can consider
    // showing an upsell message to the user. The upsell message containing
    // information on withdrawal
    private boolean isInUpsellState() {
        return  !hasWithdrawn && numberOfRewards == 3;
    }

    private void emitNewBalance() {
        if (isInUpsellState()) {
            this.upsellSubject.onCompleted();
        }
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