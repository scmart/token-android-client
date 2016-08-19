package com.bakkenbaeck.toshi.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.toshi.R;
import com.bakkenbaeck.toshi.model.Message;
import com.bakkenbaeck.toshi.model.RemoteVideoMessage;
import com.bakkenbaeck.toshi.view.adapter.viewholder.LocalTextViewHolder;
import com.bakkenbaeck.toshi.view.adapter.viewholder.RemoteTextViewHolder;
import com.bakkenbaeck.toshi.view.adapter.viewholder.RemoteVideoViewHolder;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.bakkenbaeck.toshi.model.Message.TYPE_LOCAL_TEXT;
import static com.bakkenbaeck.toshi.model.Message.TYPE_REMOTE_TEXT;
import static com.bakkenbaeck.toshi.model.Message.TYPE_REMOTE_VIDEO;

public final class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Message> messages;
    private final PublishSubject<Integer> onClickSubject = PublishSubject.create();

    public MessageAdapter() {
        this.messages = new ArrayList<>();
    }

    public final void addMessage(final Message message) {
        this.messages.add(message);
        notifyItemInserted(this.messages.size() - 1);
    }

    @Override
    public int getItemViewType(final int position) {
        final Message message = this.messages.get(position);
        return message.getType();
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
        final Message message = this.messages.get(position);
        switch (holder.getItemViewType()) {
            case TYPE_REMOTE_TEXT: {
                final RemoteTextViewHolder vh = (RemoteTextViewHolder) holder;
                vh.messageText.setText(message.getTextContents());
                break;
            }
            case TYPE_REMOTE_VIDEO: {
                final RemoteVideoMessage rvm = (RemoteVideoMessage) message;
                final RemoteVideoViewHolder vh = (RemoteVideoViewHolder) holder;
                if (!rvm.hasBeenViewed()) {
                    vh.videoState.setImageResource(R.drawable.ic_av_play_circle_outline);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
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
                vh.messageText.setText(message.getTextContents());
                break;
            }
        }
    }

    @Override
    public final int getItemCount() {
        return messages.size();
    }

    public Observable<Integer> getPositionClicks(){
        return onClickSubject.asObservable();
    }

    public Message getItemAt(final int clickedPosition) {
        return this.messages.get(clickedPosition);
    }
}
