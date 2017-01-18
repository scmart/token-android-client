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
    private String text;
    private boolean sentByLocal;

    public ChatMessage() {
        this.creationTime = System.currentTimeMillis();
    }

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

    public ChatMessage setText(final String text) {
        this.text = text;
        return this;
    }

    public ChatMessage setSentByLocal(final boolean sentByLocal) {
        this.sentByLocal = sentByLocal;
        return this;
    }

    public long getCreationTime(){
        return creationTime;
    }

    public String getText() {
        return this.text;
    }

    public String getSofaPayload() {
        final String regexString = "\\{.*?\\}";
        final Pattern pattern = Pattern.compile(regexString);
        final Matcher m = pattern.matcher(this.text);
        if (m.find()) {
            return m.group();
        }
        return null;
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

    public ChatMessage makeTextMessage(
            final String conversationId,
            final boolean sentByLocal,
            final String text) {
        return
                setConversationId(conversationId)
                        .setSendState(SendState.STATE_SENDING)
                        .setType(SofaType.PLAIN_TEXT)
                        .setSentByLocal(sentByLocal)
                        .setText(text);
    }

    public ChatMessage makeLocalPaymentRequest(final String conversationId, final String sofaPayload) {
        return
                setConversationId(conversationId)
                        .setSendState(SendState.STATE_SENDING)
                        .setType(SofaType.PAYMENT_REQUEST)
                        .setSentByLocal(true)
                        .setText(sofaPayload);
    }
}
