package com.tokenbrowser.manager.store;


import com.tokenbrowser.model.local.PendingTransaction;

import io.realm.Realm;
import io.realm.RealmQuery;
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

    public Single<PendingTransaction> load(final String txHash) {
        return Single.fromCallable(() -> loadWhere("txHash", txHash));
    }

    private PendingTransaction loadWhere(final String fieldName, final String value) {
        final Realm realm = Realm.getDefaultInstance();
        final RealmQuery<PendingTransaction> query = realm
                        .where(PendingTransaction.class)
                        .equalTo(fieldName, value);

        final PendingTransaction pendingTransaction = query.findFirst();
        final PendingTransaction retVal = pendingTransaction == null ? null : realm.copyFromRealm(pendingTransaction);
        realm.close();
        return retVal;
    }

    private void broadcastPendingTransaction(final PendingTransaction pendingTransaction) {
        this.pendingTransactionObservable.onNext(pendingTransaction);
    }
}
