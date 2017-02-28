package com.bakkenbaeck.token.manager.store;


import com.bakkenbaeck.token.model.local.PendingTransaction;

import io.realm.Realm;
import io.realm.RealmQuery;
import rx.Single;
import rx.subjects.PublishSubject;

public class PendingTransactionStore {

    private final Realm realm;
    private final PublishSubject<PendingTransaction> pendingTransactionObservable;

    public PendingTransactionStore() {
        this.realm = Realm.getDefaultInstance();
        this.pendingTransactionObservable = PublishSubject.create();
    }

    public PublishSubject<PendingTransaction> getPendingTransactionObservable() {
        return pendingTransactionObservable;
    }

    public void save(final PendingTransaction pendingTransaction) {
        this.realm.beginTransaction();
        this.realm.insertOrUpdate(pendingTransaction);
        this.realm.commitTransaction();
        broadcastPendingTransaction(pendingTransaction);
    }

    public Single<PendingTransaction> load(final String txHash) {
        return Single.fromCallable(() -> loadWhere("txHash", txHash));
    }

    private PendingTransaction loadWhere(final String fieldName, final String value) {
        final RealmQuery<PendingTransaction> query = realm
                        .where(PendingTransaction.class)
                        .equalTo(fieldName, value);

        final PendingTransaction pendingTransaction = query.findFirst();
        if (pendingTransaction == null) return null;
        return realm.copyFromRealm(pendingTransaction);
    }

    private void broadcastPendingTransaction(final PendingTransaction pendingTransaction) {
        this.pendingTransactionObservable.onNext(pendingTransaction);
    }

    private void close() {
        this.realm.close();
    }
}
