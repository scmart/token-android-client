package com.bakkenbaeck.token.manager;


import com.bakkenbaeck.token.model.LocalBalance;
import com.bakkenbaeck.token.network.rest.model.TransactionSent;
import com.bakkenbaeck.token.network.ws.model.TransactionConfirmation;

import rx.Observable;
import rx.subjects.BehaviorSubject;

public class BalanceManager {

    private final BehaviorSubject<LocalBalance> balanceSubject = BehaviorSubject.create();

    private LocalBalance localBalance;

    public BalanceManager init() {
        this.localBalance = new LocalBalance();
        return this;
    }

    public Observable<LocalBalance> getObservable() {
        return this.balanceSubject.asObservable();
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