package com.bakkenbaeck.toshi.manager;


import com.bakkenbaeck.toshi.model.LocalBalance;
import com.bakkenbaeck.toshi.model.User;
import com.bakkenbaeck.toshi.network.rest.model.TransactionSent;
import com.bakkenbaeck.toshi.network.ws.model.TransactionConfirmation;
import com.bakkenbaeck.toshi.util.OnNextObserver;
import com.bakkenbaeck.toshi.view.BaseApplication;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class LocalBalanceManager {

    private final BehaviorSubject<LocalBalance> balanceSubject = BehaviorSubject.create();

    private LocalBalance localBalance;

    public LocalBalanceManager() {
        this.localBalance = new LocalBalance();
        initBalanceListeners();
    }

    private void initBalanceListeners() {
        BaseApplication.get().getUserManager().getObservable().subscribe(this.currentUserSubscriber);
        BaseApplication.get().getSocketObservables().getTransactionConfirmationObservable().subscribe(this.newTransactionConfirmationSubscriber);
        BaseApplication.get().getSocketObservables().getTransactionSentObservable().subscribe(this.newTransactionSentSubscriber);
    }

    private final OnNextObserver<User> currentUserSubscriber = new OnNextObserver<User>() {
        @Override
        public void onNext(final User user) {
            setBalance(user);
        }
    };

    private final OnNextObserver<TransactionConfirmation> newTransactionConfirmationSubscriber = new OnNextObserver<TransactionConfirmation>() {
        @Override
        public void onNext(final TransactionConfirmation confirmation) {
            setBalance(confirmation);
        }
    };

    private final OnNextObserver<TransactionSent> newTransactionSentSubscriber = new OnNextObserver<TransactionSent>() {
        @Override
        public void onNext(final TransactionSent transactionSent) {
            setBalance(transactionSent);
        }
    };

    public Observable<LocalBalance> getObservable() {
        return this.balanceSubject.asObservable();
    }

    private void setBalance(final User user) {
        this.localBalance.setUnconfirmedBalance(user.getUnconfirmedBalance());
        this.localBalance.setConfirmedBalance(user.getConfirmedBalance());
        emitNewBalance();
    }

    private void setBalance(final TransactionConfirmation confirmation) {
        this.localBalance.setUnconfirmedBalance(confirmation.getUnconfirmedBalance());
        this.localBalance.setConfirmedBalance(confirmation.getConfirmedBalance());
        emitNewBalance();
    }

    private void setBalance(final TransactionSent transactionSent) {
        this.localBalance.setUnconfirmedBalance(transactionSent.getUnconfirmedBalance());
        this.localBalance.setConfirmedBalance(transactionSent.getConfirmedBalance());
        emitNewBalance();
    }

    private void emitNewBalance() {
        this.balanceSubject.onNext(this.localBalance);
    }

    @Override
    public String toString() {
        return this.localBalance.unconfirmedBalanceString();
    }
}