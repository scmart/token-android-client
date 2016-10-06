package com.bakkenbaeck.toshi.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.toshi.R;
import com.bakkenbaeck.toshi.model.ChatMessage;
import com.bakkenbaeck.toshi.util.OnSingleClickListener;
import com.bakkenbaeck.toshi.view.adapter.viewholder.LocalTextViewHolder;
import com.bakkenbaeck.toshi.view.adapter.viewholder.RemoteTextViewHolder;
import com.bakkenbaeck.toshi.view.adapter.viewholder.RemoteVideoViewHolder;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.bakkenbaeck.toshi.model.ChatMessage.TYPE_LOCAL_TEXT;
import static com.bakkenbaeck.toshi.model.ChatMessage.TYPE_REMOTE_TEXT;
import static com.bakkenbaeck.toshi.model.ChatMessage.TYPE_REMOTE_VIDEO;

public final class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> chatMessages;
    private List<ChatMessage> chatMessagesWhilstPaused;

    private final PublishSubject<Integer> onClickSubject = PublishSubject.create();

    private boolean isRenderingPaused;

    public MessageAdapter() {
        this.chatMessages = new ArrayList<>();
        this.chatMessagesWhilstPaused = new ArrayList<>();
    }

    public final void addMessage(final ChatMessage chatMessage) {
        if (isRenderingPaused) {
            this.chatMessagesWhilstPaused.add(chatMessage);
        } else {
            addAndRenderMessage(chatMessage);
        }
    }

    private void addAndRenderMessage(final ChatMessage chatMessage) {
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
            case TYPE_REMOTE_VIDEO: {
                final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__remote_video_message, parent, false);
                return new RemoteVideoViewHolder(v);
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
                vh.messageText.setText(chatMessage.getText());
                break;
            }
            case TYPE_REMOTE_VIDEO: {
                final RemoteVideoViewHolder vh = (RemoteVideoViewHolder) holder;
                if (!chatMessage.hasBeenWatched()) {
                    vh.videoState.setImageResource(R.drawable.ic_av_play_circle_outline);
                    holder.itemView.setOnClickListener(new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            onClickSubject.onNext(holder.getAdapterPosition());
                        }
                    });
                } else {
                    holder.itemView.setOnClickListener(null);
                    vh.videoState.setImageResource(R.drawable.ic_action_watched);
                }

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

    public Observable<Integer> getPositionClicks(){
        return this.onClickSubject.asObservable();
    }

    public ChatMessage getItemAt(final int clickedPosition) {
        return this.chatMessages.get(clickedPosition);
    }

    public void pauseRendering() {
        this.isRenderingPaused = true;
    }

    public void unPauseRendering() {
        this.isRenderingPaused = false;
        for (final ChatMessage chatMessage : this.chatMessagesWhilstPaused) {
            addAndRenderMessage(chatMessage);
        }
        this.chatMessagesWhilstPaused.clear();
    }
}
