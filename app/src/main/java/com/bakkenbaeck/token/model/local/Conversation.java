package com.bakkenbaeck.token.model.local;


import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Conversation extends RealmObject {

    @PrimaryKey
    private String conversationId;
    private User member;
    private SofaMessage latestMessage;
    private long updatedTime;
    private RealmList<SofaMessage> allMessages;
    private int numberOfUnread;

    public Conversation() {}

    public Conversation(final User user) {
        this.member = user;
        this.conversationId = user.getOwnerAddress();
    }

    public User getMember() {
        return member;
    }

    public SofaMessage getLatestMessage() {
        return latestMessage;
    }

    public Conversation setLatestMessage(final SofaMessage latestMessage) {
        this.latestMessage = latestMessage;
        this.updatedTime = latestMessage.getCreationTime();
        addMessage(latestMessage);
        return this;
    }

    private void addMessage(final SofaMessage latestMessage) {
        if (this.allMessages == null) {
            this.allMessages = new RealmList<>();
        }
        this.allMessages.add(latestMessage);
    }

    public List<SofaMessage> getAllMessages() {
        return allMessages;
    }

    public int getNumberOfUnread() {
        return numberOfUnread;
    }

    public void setNumberOfUnread(final int numberOfUnread) {
        this.numberOfUnread = numberOfUnread;
    }

    @Override
    public boolean equals(Object other){
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Conversation))return false;
        final Conversation otherConversationMessage = (Conversation) other;
        return otherConversationMessage.getConversationId().equals(this.conversationId);
    }

    @Override
    public int hashCode() {
        return conversationId.hashCode();
    }

    private String getConversationId() {
        return conversationId;
    }
}
