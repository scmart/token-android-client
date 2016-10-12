package com.bakkenbaeck.toshi.model;

import android.support.annotation.IntDef;

import com.bakkenbaeck.toshi.network.ws.model.Action;
import com.bakkenbaeck.toshi.network.ws.model.Detail;
import com.bakkenbaeck.toshi.network.ws.model.Message;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class ChatMessage extends RealmObject {

    @IntDef({
            TYPE_LOCAL_TEXT,
            TYPE_REMOTE_TEXT,
            TYPE_REMOTE_VIDEO,
            TYPE_REMOTE_VERIFICATION,
            TYPE_DAY
    })
    private @interface Type {}
    @Ignore public static final int TYPE_LOCAL_TEXT = 0;
    @Ignore public static final int TYPE_REMOTE_TEXT = 1;
    @Ignore public static final int TYPE_REMOTE_VIDEO = 2;
    @Ignore public static final int TYPE_REMOTE_VERIFICATION = 3;
    @Ignore public static final int TYPE_DAY = 4;

    @Ignore public static final String VERIFICATION_TYPE = "verification_reminder";
    @Ignore public static final String REWARD_EARNED_TYPE = "rewards_earned";

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

    public List<Action> getAction(){
        return actions;
    }

    public ChatMessage setActions(List<Action> actions){
        if(this.actions == null){
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

    public ChatMessage makeRemoteVideoMessage(String text) {
        setType(TYPE_REMOTE_VIDEO);
        setText(text);
        return this;
    }

    public ChatMessage makeRemoteRewardMessage(Message message){
        setType(TYPE_REMOTE_TEXT);
        setText(message.toString());
        setDetails(message.getDetails());
        return this;
    }

    public ChatMessage makeRemoteVerificationMessage(Message message){
        setType(TYPE_REMOTE_VERIFICATION);
        setText(message.toString());
        setActions(message.getActions());
        return this;
    }

    public ChatMessage makeRemoteVerificationMessageSuccess(String message, int reputationGained){
        setType(TYPE_REMOTE_VERIFICATION);

        List<Detail> details = new ArrayList<Detail>();
        details.add(new Detail("reputation_gained", reputationGained));

        setDetails(details);
        setText(message);
        return this;
    }

    public ChatMessage makeDayMessage(){
        setType(TYPE_DAY);
        return this;
    }
}
