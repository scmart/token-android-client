package com.bakkenbaeck.token.model.local;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PendingTransaction extends RealmObject {

    @PrimaryKey
    private String txHash;
    private ChatMessage chatMessage;

    public PendingTransaction() {}

    public String getTxHash() {
        return txHash;
    }

    public PendingTransaction setTxHash(final String txHash) {
        this.txHash = txHash;
        return this;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public PendingTransaction setChatMessage(final ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
        return this;
    }
}
