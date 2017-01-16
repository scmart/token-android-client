package com.bakkenbaeck.token.model.local;

import android.support.annotation.IntDef;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class ChatMessage extends RealmObject {

    @PrimaryKey
    private String privateKey;

    @IntDef({
            TYPE_LOCAL_TEXT,
            TYPE_REMOTE_TEXT,
            TYPE_DAY
    })
    private @interface Type {}
    @Ignore public static final int TYPE_LOCAL_TEXT = 0;
    @Ignore public static final int TYPE_REMOTE_TEXT = 1;
    @Ignore public static final int TYPE_DAY = 2;

    @IntDef({
            STATE_SENDING,
            STATE_SENT,
            STATE_FAILED,
            STATE_RECEIVED
    })
    public @interface SendState {}
    @Ignore public static final int STATE_SENDING = 0;
    @Ignore public static final int STATE_SENT = 1;
    @Ignore public static final int STATE_FAILED = 2;
    @Ignore public static final int STATE_RECEIVED = 3;

    private long creationTime;
    private @Type int type;
    private @SendState int sendState;
    private String conversationId;
    private String text;

    public ChatMessage() {
        this.creationTime = System.currentTimeMillis();
    }

    private ChatMessage setType(final @Type int type) {
        this.type = type;
        return this;
    }

    public ChatMessage setSendState(final @SendState int sendState) {
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

    public long getCreationTime(){
        return creationTime;
    }

    public String getText() {
        return this.text;
    }

    public String getConversationId() {
        return this.conversationId;
    }

    public @Type int getType() {
        return this.type;
    }

    public @SendState int getSendState() {
        return this.sendState;
    }

    // Helper functions

    public ChatMessage makeLocalMessage(final String conversationId, final String text) {
        return
            setConversationId(conversationId)
                    .setSendState(STATE_SENDING)
                    .setType(TYPE_LOCAL_TEXT)
                    .setText(text);
    }

    public ChatMessage makeRemoteMessage(final String conversationId, final String text) {
        return
            setConversationId(conversationId)
                .setSendState(STATE_RECEIVED)
                .setType(TYPE_REMOTE_TEXT)
                .setText(text);
    }

    public ChatMessage makeDayHeader(){
        setType(TYPE_DAY);
        return this;
    }
}
