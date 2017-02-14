package com.bakkenbaeck.token.manager.model;


import android.support.annotation.IntDef;

import com.bakkenbaeck.token.model.local.ChatMessage;
import com.bakkenbaeck.token.model.local.User;

public class ChatMessageTask {


    @IntDef({SEND_AND_SAVE, SAVE_ONLY, SEND_ONLY})
    public @interface Action {}
    public static final int SEND_AND_SAVE = 0;
    public static final int SAVE_ONLY = 1;
    public static final int SEND_ONLY = 2;

    private final User receiver;
    private final ChatMessage chatMessage;
    private final @Action int action;

    public ChatMessageTask(
            final User receiver,
            final ChatMessage chatMessage,
            final @Action int action) {
        this.receiver = receiver;
        this.chatMessage = chatMessage;
        this.action = action;
    }

    public User getReceiver() {
        return receiver;
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public int getAction() {
        return action;
    }
}
