package com.bakkenbaeck.token.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.ChatMessage;
import com.bakkenbaeck.token.util.DateUtil;
import com.bakkenbaeck.token.util.MessageUtil;
import com.bakkenbaeck.token.view.adapter.viewholder.DayViewHolder;
import com.bakkenbaeck.token.view.adapter.viewholder.LocalTextViewHolder;
import com.bakkenbaeck.token.view.adapter.viewholder.RemoteTextViewHolder;

import java.util.ArrayList;
import java.util.List;

import static com.bakkenbaeck.token.model.ChatMessage.TYPE_DAY;
import static com.bakkenbaeck.token.model.ChatMessage.TYPE_LOCAL_TEXT;
import static com.bakkenbaeck.token.model.ChatMessage.TYPE_REMOTE_TEXT;

public final class  MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatMessage> chatMessages;

    public MessageAdapter() {
        this.chatMessages = new ArrayList<>();
    }

    public final void addMessage(final ChatMessage chatMessage) {
        this.chatMessages.add(chatMessage);
        notifyItemInserted(this.chatMessages.size() - 1);
    }

    @Override
    public int getItemViewType(final int position) {
        final ChatMessage chatMessage = this.chatMessages.get(position);
        return chatMessage.getType();
    }

    @Override
    public final RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        switch (viewType) {
            case TYPE_REMOTE_TEXT: {
                final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__remote_text_message, parent, false);
                return new RemoteTextViewHolder(v);
            }

            case TYPE_DAY: {
                final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__day_message, parent, false);
                return new DayViewHolder(v);
            }
            case TYPE_LOCAL_TEXT:
            default: {
                final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__local_text_message, parent, false);
                return new LocalTextViewHolder(v);
            }
        }
    }

    @Override
    public final void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final ChatMessage chatMessage = this.chatMessages.get(position);
        switch (holder.getItemViewType()) {
            case TYPE_REMOTE_TEXT: {
                final RemoteTextViewHolder vh = (RemoteTextViewHolder) holder;
                final String parsedMessage = MessageUtil.parseString(chatMessage.getText());
                vh.messageText.setText(parsedMessage);
                break;
            }
            case TYPE_DAY: {
                final DayViewHolder vh = (DayViewHolder) holder;
                chatMessage.getCreationTime();
                final String formattedDate = DateUtil.getDate("EEEE", chatMessage.getCreationTime());
                vh.date.setText(formattedDate);
                break;
            }
            case TYPE_LOCAL_TEXT:
            default: {
                final LocalTextViewHolder vh = (LocalTextViewHolder) holder;
                vh.messageText.setText(chatMessage.getText());
                break;
            }
        }
    }

    @Override
    public final int getItemCount() {
        return this.chatMessages.size();
    }
}
