package com.bakkenbaeck.token.model.local;


import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Conversation extends RealmObject {

    @PrimaryKey
    private String conversationId;
    private User member;
    private ChatMessage latestMessage;
    private RealmList<ChatMessage> allMessages;
    private int numberOfUnread;

    public Conversation() {}

    public Conversation(final User user) {
        this.member = user;
        this.conversationId = this.member.getOwnerAddress();
    }

    public User getMember() {
        return member;
    }

    public ChatMessage getLatestMessage() {
        return latestMessage;
    }

    public Conversation setLatestMessage(final ChatMessage latestMessage) {
        this.latestMessage = latestMessage;
        return this;
    }

    public RealmList<ChatMessage> getAllMessages() {
        return allMessages;
    }

    public int getNumberOfUnread() {
        return numberOfUnread;
    }

}
