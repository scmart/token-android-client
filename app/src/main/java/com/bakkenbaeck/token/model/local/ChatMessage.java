package com.bakkenbaeck.token.model.local;

import com.bakkenbaeck.token.model.sofa.SofaType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ChatMessage extends RealmObject {

    @PrimaryKey
    private String privateKey;
    private long creationTime;
    private @SofaType.Type int type;
    private @SendState.State int sendState;
    private String conversationId;
    private String payload;
    private boolean sentByLocal;

    public ChatMessage() {
        this.creationTime = System.currentTimeMillis();
    }

    // Setters

    private ChatMessage setType(final @SofaType.Type int type) {
        this.type = type;
        return this;
    }

    public ChatMessage setSendState(final @SendState.State int sendState) {
        this.sendState = sendState;
        return this;
    }

    private ChatMessage setConversationId(final String conversationId) {
        this.conversationId = conversationId;
        this.privateKey = this.conversationId + String.valueOf(this.creationTime);
        return this;
    }

    public ChatMessage setPayload(final String payload) {
        this.payload = payload;
        return this;
    }

    public ChatMessage setSentByLocal(final boolean sentByLocal) {
        this.sentByLocal = sentByLocal;
        return this;
    }

    // Getters

    public String getPayload() {
        return cleanPayload(this.payload);
    }

    public String getPayloadWithHeader() {
        return this.payload;
    }

    public String getConversationId() {
        return this.conversationId;
    }

    public @SofaType.Type int getType() {
        return this.type;
    }

    public @SendState.State int getSendState() {
        return this.sendState;
    }

    public boolean isSentByLocal() {
        return this.sentByLocal;
    }

    // Helper functions

    private String cleanPayload(final String payload) {
        final String regexString = "\\{.*?\\}";
        final Pattern pattern = Pattern.compile(regexString);
        final Matcher m = pattern.matcher(payload);
        if (m.find()) {
            return m.group();
        }
        return payload;
    }

    public ChatMessage makeNew(
            final String conversationId,
            final @SofaType.Type int type,
            final boolean sentByLocal,
            final String sofaPayload) {
        return
                setConversationId(conversationId)
                        .setSendState(SendState.STATE_SENDING)
                        .setType(type)
                        .setSentByLocal(sentByLocal)
                        .setPayload(sofaPayload);
    }
}
