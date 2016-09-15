package com.bakkenbaeck.toshi.manager;


import com.bakkenbaeck.toshi.model.LocalBalance;
import com.bakkenbaeck.toshi.model.User;
import com.bakkenbaeck.toshi.network.ws.model.Payment;
import com.bakkenbaeck.toshi.util.OnNextObserver;
import com.bakkenbaeck.toshi.view.BaseApplication;

import rx.Observable;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

public class LocalBalanceManager {

    private final BehaviorSubject<LocalBalance> balanceSubject = BehaviorSubject.create();
    private final PublishSubject<Void> upsellSubject = PublishSubject.create();

    private LocalBalance localBalance;
    private boolean hasWithdrawn = false;
    private int numberOfRewards = 0;

    public LocalBalanceManager() {
        this.localBalance = new LocalBalance();
        initBalanceListeners();
    }

    private void initBalanceListeners() {
        BaseApplication.get().getUserManager().getObservable().subscribe(this.currentUserSubscriber);
        BaseApplication.get().getSocketObservables().getPaymentObservable().subscribe(this.newPaymentSubscriber);
    }

    private final OnNextObserver<User> currentUserSubscriber = new OnNextObserver<User>() {
        @Override
        public void onNext(final User user) {
            setBalance(user);
        }
    };

    private final OnNextObserver<Payment> newPaymentSubscriber = new OnNextObserver<Payment>() {
        @Override
        public void onNext(final Payment payment) {
            setBalance(payment);
        }
    };

    public Observable<LocalBalance> getObservable() {
        return this.balanceSubject.asObservable();
    }

    public Observable<Void> getUpsellObservable() {
        return this.upsellSubject.asObservable();
    }

    private void setBalance(final Payment payment) {
        ++numberOfRewards;
        this.localBalance.setUnconfirmedBalance(payment.getUnconfirmedBalance());
        this.localBalance.setConfirmedBalance(payment.getConfirmedBalance());
        emitNewBalance();
    }

    private void setBalance(final User user) {
        this.localBalance.setUnconfirmedBalance(user.getUnconfirmedBalance());
        this.localBalance.setConfirmedBalance(user.getConfirmedBalance());
        emitNewBalance();
    }
    
/*
    TODO
    public void subtract(final BigInteger amount) {
        this.amountInWei = this.amountInWei.subtract(amount);
        this.hasWithdrawn = true;
    }
*/

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
        this.balanceSubject.onNext(this.localBalance);
    }

    @Override
    public String toString() {
        return this.localBalance.unconfirmedBalanceString();
    }
}