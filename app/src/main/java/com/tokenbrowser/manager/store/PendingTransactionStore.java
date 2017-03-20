package com.tokenbrowser.manager.store;


import com.tokenbrowser.model.local.PendingTransaction;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import rx.Observable;
import rx.Single;
import rx.subjects.PublishSubject;

public class PendingTransactionStore {
    private final PublishSubject<PendingTransaction> pendingTransactionObservable;

    public PendingTransactionStore() {
        this.pendingTransactionObservable = PublishSubject.create();
    }

    public PublishSubject<PendingTransaction> getPendingTransactionObservable() {
        return pendingTransactionObservable;
    }

    public void save(final PendingTransaction pendingTransaction) {
        final Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.insertOrUpdate(pendingTransaction);
        realm.commitTransaction();
        realm.close();
        broadcastPendingTransaction(pendingTransaction);
    }

    public Single<PendingTransaction> loadTransaction(final String txHash) {
        return Single.fromCallable(() -> loadSingleWhere("txHash", txHash));
    }

    public Observable<PendingTransaction> loadAllTransactions() {
        return Observable.from(loadAll());
    }

    private PendingTransaction loadSingleWhere(final String fieldName, final String value) {
        final Realm realm = Realm.getDefaultInstance();
        final RealmQuery<PendingTransaction> query = realm
                .where(PendingTransaction.class)
                .equalTo(fieldName, value);

        final PendingTransaction pendingTransaction = query.findFirst();
        final PendingTransaction retVal = pendingTransaction == null ? null : realm.copyFromRealm(pendingTransaction);
        realm.close();
        return retVal;
    }

    private List<PendingTransaction> loadAll() {
        final Realm realm = Realm.getDefaultInstance();
        final RealmQuery<PendingTransaction> query = realm
                .where(PendingTransaction.class);

        final List<PendingTransaction> pendingTransactions = query.findAll();
        final List<PendingTransaction> retVal = realm.copyFromRealm(pendingTransactions);
        realm.close();
        return retVal;
    }


    private void broadcastPendingTransaction(final PendingTransaction pendingTransaction) {
        this.pendingTransactionObservable.onNext(pendingTransaction);
    }
}
