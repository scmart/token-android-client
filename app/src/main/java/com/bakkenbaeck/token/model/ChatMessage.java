package com.bakkenbaeck.token.model;

import android.support.annotation.IntDef;

import com.bakkenbaeck.token.network.ws.model.Action;
import com.bakkenbaeck.token.network.ws.model.Detail;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class ChatMessage extends RealmObject {

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
    private RealmList<Detail> details;
    private RealmList<Action> actions;

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

    public ChatMessage setConversationId(final String conversationId) {
        this.conversationId = conversationId;
        return this;
    }

    public ChatMessage setText(final String text) {
        this.text = text;
        return this;
    }

    public ChatMessage setActions(final List<Action> actions){
        if (this.actions == null) {
            this.actions = new RealmList<>();
        }
        this.actions.clear();
        this.actions.addAll(actions);
        return this;
    }

    public ChatMessage setDetails(List<Detail> details){
        if(this.details == null){
            this.details = new RealmList<>();
        }
        this.details.clear();
        this.details.addAll(details);
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

    public List<Detail> getDetails(){
        return this.details;
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
