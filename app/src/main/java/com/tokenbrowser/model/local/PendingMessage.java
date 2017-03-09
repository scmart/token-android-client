package com.tokenbrowser.model.local;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PendingMessage extends RealmObject {

    @PrimaryKey
    private String privateKey;
    private User receiver;
    private SofaMessage sofaMessage;

    public PendingMessage() {}

    public User getReceiver() {
        return receiver;
    }

    public PendingMessage setReceiver(final User receiver) {
        this.receiver = receiver;
        return this;
    }

    public SofaMessage getSofaMessage() {
        return sofaMessage;
    }

    public PendingMessage setSofaMessage(final SofaMessage sofaMessage) {
        this.sofaMessage = sofaMessage;
        this.privateKey = sofaMessage.getPrivateKey();
        return this;
    }
}
