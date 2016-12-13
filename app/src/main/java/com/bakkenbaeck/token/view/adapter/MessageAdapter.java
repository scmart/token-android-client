package com.bakkenbaeck.token.view.adapter;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.ChatMessage;
import com.bakkenbaeck.token.util.DateUtil;
import com.bakkenbaeck.token.util.MessageUtil;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.view.adapter.viewholder.DayViewHolder;
import com.bakkenbaeck.token.view.adapter.viewholder.LocalTextViewHolder;
import com.bakkenbaeck.token.view.adapter.viewholder.RemoteTextViewHolder;
import com.bakkenbaeck.token.view.adapter.viewholder.RemoteVideoViewHolder;

import java.util.ArrayList;
import java.util.List;

import rx.subjects.PublishSubject;

import static com.bakkenbaeck.token.model.ChatMessage.TYPE_DAY;
import static com.bakkenbaeck.token.model.ChatMessage.TYPE_LOCAL_TEXT;
import static com.bakkenbaeck.token.model.ChatMessage.TYPE_REMOTE_TEXT;
import static com.bakkenbaeck.token.model.ChatMessage.TYPE_REMOTE_VIDEO;

public final class  MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ChatMessage> chatMessages;
    private List<ChatMessage> chatMessagesWhilstPaused;

    private final PublishSubject<Integer> onClickSubject = PublishSubject.create();

    private boolean isRenderingPaused;
    private Activity activity;
    private TextView verifyButton;

    public MessageAdapter(Activity activity) {
        this.chatMessages = new ArrayList<>();
        this.chatMessagesWhilstPaused = new ArrayList<>();
        this.activity = activity;
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
        notifyItemChanged(this.chatMessages.size() - 2);
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
                vh.verificationButton.setVisibility(View.GONE);

                //Details reward
                if(chatMessage.getDetails() != null && chatMessage.getDetails().size() > 0) {
                    vh.details.setVisibility(View.VISIBLE);

                    //Earned
                    vh.earnedTotalWrapper.setVisibility(View.GONE);
                    if (chatMessage.getDetails().size() == 1) {
                        vh.earnedWrapper.setBackground(ContextCompat.getDrawable(activity, R.drawable.reward_background));
                    } else {
                        vh.earnedWrapper.setBackground(ContextCompat.getDrawable(activity, R.drawable.top_radius_background));
                    }
                    vh.earnedWrapper.setVisibility(View.VISIBLE);
                    String subString1 = String.valueOf(chatMessage.getDetails().get(0).getValue());
                    vh.earned.setText(chatMessage.getDetails().get(0).getTitle());
                    vh.earnedValue.setText(subString1);

                    //Total
                    if (chatMessage.getDetails().size() > 1) {
                        vh.earnedTotalWrapper.setVisibility(View.VISIBLE);
                        String subString2 = String.valueOf(chatMessage.getDetails().get(1).getValue());
                        vh.earnedTotal.setText(chatMessage.getDetails().get(1).getTitle());
                        vh.earnedTotalValue.setText(subString2);
                    }
                }
                break;
            }
            case TYPE_REMOTE_VIDEO: {
                final RemoteVideoViewHolder vh = (RemoteVideoViewHolder) holder;
                vh.videoBackground.setBackground(ContextCompat.getDrawable(activity, R.drawable.background_video));
                vh.videoState.setImageResource(R.drawable.play_vec);
                vh.title.setText(chatMessage.getText());
                vh.watched.setVisibility(View.GONE);
                holder.itemView.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        onClickSubject.onNext(holder.getAdapterPosition());
                    }
                });

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

    public void clean(){
        activity = null;
        verifyButton = null;
    }
}
