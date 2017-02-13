package com.bakkenbaeck.token.model.local;


import io.realm.RealmList;
import io.realm.RealmObject;

public class Conversation extends RealmObject {

    private User member;
    private ChatMessage latestMessage;
    private RealmList<ChatMessage> allMessages;
    private int numberOfUnread;

    public Conversation() {}

    public Conversation(final User user) {
        this.member = user;
    }

    public User getMember() {
        return member;
    }

    public ChatMessage getLatestMessage() {
        return latestMessage;
    }

    public RealmList<ChatMessage> getAllMessages() {
        return allMessages;
    }

    public int getNumberOfUnread() {
        return numberOfUnread;
    }
}
