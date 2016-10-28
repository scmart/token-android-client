package com.bakkenbaeck.token.model;

import android.support.annotation.IntDef;

import com.bakkenbaeck.token.network.ws.model.Action;
import com.bakkenbaeck.token.network.ws.model.Detail;
import com.bakkenbaeck.token.network.ws.model.Message;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class ChatMessage extends RealmObject {

    @IntDef({
            TYPE_LOCAL_TEXT,
            TYPE_REMOTE_TEXT,
            TYPE_REMOTE_VIDEO,
            TYPE_DAY
    })
    private @interface Type {}
    @Ignore public static final int TYPE_LOCAL_TEXT = 0;
    @Ignore public static final int TYPE_REMOTE_TEXT = 1;
    @Ignore public static final int TYPE_REMOTE_VIDEO = 2;
    @Ignore public static final int TYPE_DAY = 4;

    @Ignore public static final String REWARD_EARNED_TYPE = "rewards_earned";
    @Ignore public static final String DAILY_LIMIT_REACHED = "daily_limit_reached";

    private long creationTime;
    private @Type int type;
    private String text;
    private RealmList<Detail> details;
    private RealmList<Action> actions;
    private boolean hasBeenWatched = false;

    public ChatMessage() {
        this.creationTime = System.currentTimeMillis();
    }

    private ChatMessage setType(final @Type int type) {
        this.type = type;
        return this;
    }

    public ChatMessage setText(final String text) {
        this.text = text;
        return this;
    }

    public List<Detail> getDetails(){
        return this.details;
    }

    public boolean shouldShowVerifyButton(){
        return actions.size() == 1 && actions.get(0).getAction().equals("verify_phone_number");
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

    @Type
    public int getType() {
        return this.type;
    }

    public void markAsWatched() {
        this.hasBeenWatched = true;
    }

    public boolean hasBeenWatched() {
        return hasBeenWatched;
    }


    // Helper functions

    public ChatMessage makeLocalMessageWithText(final String text) {
        setType(TYPE_LOCAL_TEXT);
        setText(text);
        return this;
    }

    public ChatMessage makeRemoteMessageWithText(final String text) {
        setType(TYPE_REMOTE_TEXT);
        setText(text);
        return this;
    }

    public ChatMessage makeRemoteVideoMessage(final String text) {
        setType(TYPE_REMOTE_VIDEO);
        setText(text);
        return this;
    }

    public ChatMessage makeRemoteRewardMessage(Message message){
        setType(TYPE_REMOTE_TEXT);
        setText(message.toString());
        setDetails(message.getDetails());
        setActions(message.getActions());
        return this;
    }

    public ChatMessage makeDayHeader(){
        setType(TYPE_DAY);
        return this;
    }
}
