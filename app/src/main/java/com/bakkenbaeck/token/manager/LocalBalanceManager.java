package com.bakkenbaeck.token.manager;


import com.bakkenbaeck.token.model.LocalBalance;
import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.network.rest.model.TransactionSent;
import com.bakkenbaeck.token.network.ws.model.TransactionConfirmation;
import com.bakkenbaeck.token.util.OnNextObserver;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class LocalBalanceManager {

    private final BehaviorSubject<LocalBalance> balanceSubject = BehaviorSubject.create();
    private final BehaviorSubject<Integer> levelSubject = BehaviorSubject.create();

    private LocalBalance localBalance;

    public LocalBalanceManager() {
        this.localBalance = new LocalBalance();
        initBalanceListeners();
    }

    private void initBalanceListeners() {
        BaseApplication.get().getUserManager()
                .getObservable()
                .subscribeOn(Schedulers.io())
                .subscribe(this.currentUserSubscriber);
        BaseApplication.get().getSocketObservables().getTransactionConfirmationObservable().subscribe(this.newTransactionConfirmationSubscriber);
        BaseApplication.get().getSocketObservables().getTransactionSentObservable().subscribe(this.newTransactionSentSubscriber);
    }

    private final SingleSuccessSubscriber<User> currentUserSubscriber = new SingleSuccessSubscriber<User>() {
        @Override
        public void onSuccess(final User user) {
            emitNewLevel(user.getLevel());
            setBalance(user);
            this.unsubscribe();
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

    public Observable<Integer> getLevelObservable(){
        return this.levelSubject.asObservable().observeOn(AndroidSchedulers.mainThread());
    }

    private void setBalance(final User user) {
        this.localBalance.setUnconfirmedBalance(user.getUnconfirmedBalance());
        this.localBalance.setConfirmedBalance(user.getConfirmedBalance());
        this.localBalance.setEthValue(user.getEthValue());
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

    private void emitNewLevel(int score){
        this.levelSubject.onNext(score);
    }

    @Override
    public String toString() {
        return this.localBalance.unconfirmedBalanceString();
    }
}