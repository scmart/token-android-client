/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.model.local;


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
        this.conversationId = user.getTokenId();
    }

    public User getMember() {
        return member;
    }

    public SofaMessage getLatestMessage() {
        return latestMessage;
    }

    public Conversation setLatestMessage(final SofaMessage latestMessage) {
        if (isDuplicateMessage(latestMessage)) {
            return this;
        }
        this.latestMessage = latestMessage;
        this.updatedTime = latestMessage.getCreationTime();
        addMessage(latestMessage);
        return this;
    }

    private boolean isDuplicateMessage(final SofaMessage message) {
        return this.allMessages != null && this.allMessages.contains(message);
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
