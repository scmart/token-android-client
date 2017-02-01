package com.bakkenbaeck.token.presenter.store;


import com.bakkenbaeck.token.model.local.PendingTransaction;

import java.util.concurrent.Callable;

import io.realm.Realm;
import io.realm.RealmQuery;
import rx.Single;

public class PendingTransactionsStore {

    private final Realm realm;

    public PendingTransactionsStore() {
        this.realm = Realm.getDefaultInstance();
    }

    public void save(final PendingTransaction pendingTransaction) {
        this.realm.beginTransaction();
        this.realm.insertOrUpdate(pendingTransaction);
        this.realm.commitTransaction();
    }

    public Single<PendingTransaction> load(final String txHash) {
        return Single.fromCallable(new Callable<PendingTransaction>() {
            @Override
            public PendingTransaction call() throws Exception {
                return loadWhere("txHash", txHash);
            }
        });
    }

    private PendingTransaction loadWhere(final String fieldName, final String value) {
        final RealmQuery<PendingTransaction> query = realm.where(PendingTransaction.class);
        query.equalTo(fieldName, value);
        return query.findFirst();
    }

    private void close() {
        this.realm.close();
    }
}
