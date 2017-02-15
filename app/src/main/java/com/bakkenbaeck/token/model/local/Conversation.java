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
        this.conversationId = user.getOwnerAddress();
    }

    public User getMember() {
        return member;
    }

    public ChatMessage getLatestMessage() {
        return latestMessage;
    }

    public Conversation setLatestMessage(final ChatMessage latestMessage) {
        this.latestMessage = latestMessage;
        addMessage(latestMessage);
        return this;
    }

    private void addMessage(final ChatMessage latestMessage) {
        if (this.allMessages == null) {
            this.allMessages = new RealmList<>();
        }
        this.allMessages.add(latestMessage);
    }

    public RealmList<ChatMessage> getAllMessages() {
        return allMessages;
    }

    public int getNumberOfUnread() {
        return numberOfUnread;
    }

    public void setNumberOfUnread(final int numberOfUnread) {
        this.numberOfUnread = numberOfUnread;
    }

}
