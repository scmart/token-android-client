package com.bakkenbaeck.token.model.local;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PendingTransaction extends RealmObject {

    @PrimaryKey
    private String txHash;
    private SofaMessage sofaMessage;

    public PendingTransaction() {}

    public String getTxHash() {
        return txHash;
    }

    public PendingTransaction setTxHash(final String txHash) {
        this.txHash = txHash;
        return this;
    }

    public SofaMessage getSofaMessage() {
        return sofaMessage;
    }

    public PendingTransaction setSofaMessage(final SofaMessage sofaMessage) {
        this.sofaMessage = sofaMessage;
        return this;
    }
}
