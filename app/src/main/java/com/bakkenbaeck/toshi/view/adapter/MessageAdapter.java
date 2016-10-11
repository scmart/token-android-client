package com.bakkenbaeck.toshi.view.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bakkenbaeck.toshi.R;
import com.bakkenbaeck.toshi.model.ChatMessage;
import com.bakkenbaeck.toshi.util.DateUtil;
import com.bakkenbaeck.toshi.util.MessageUtil;
import com.bakkenbaeck.toshi.util.OnSingleClickListener;
import com.bakkenbaeck.toshi.util.SharedPrefsUtil;
import com.bakkenbaeck.toshi.view.activity.ChatActivity;
import com.bakkenbaeck.toshi.view.adapter.viewholder.DayViewHolder;
import com.bakkenbaeck.toshi.view.adapter.viewholder.LocalTextViewHolder;
import com.bakkenbaeck.toshi.view.adapter.viewholder.RemoteTextViewHolder;
import com.bakkenbaeck.toshi.view.adapter.viewholder.RemoteVerificationViewHolder;
import com.bakkenbaeck.toshi.view.adapter.viewholder.RemoteVideoViewHolder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

import static com.bakkenbaeck.toshi.model.ChatMessage.TYPE_DAY;
import static com.bakkenbaeck.toshi.model.ChatMessage.TYPE_LOCAL_TEXT;
import static com.bakkenbaeck.toshi.model.ChatMessage.TYPE_REMOTE_TEXT;
import static com.bakkenbaeck.toshi.model.ChatMessage.TYPE_REMOTE_VERIFICATION;
import static com.bakkenbaeck.toshi.model.ChatMessage.TYPE_REMOTE_VIDEO;

public final class  MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "MessageAdapter";
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

    public interface OnVerifyClicklistener{
        void onVerifyClicked();
    }

    private OnVerifyClicklistener verifyClicklistener;

    public void setOnVerifyClickListener(OnVerifyClicklistener listener){
        verifyClicklistener = listener;
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
            case TYPE_REMOTE_VERIFICATION: {
                final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__verification_message, parent, false);
                return new RemoteVerificationViewHolder(v);
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
                vh.messageText.setText(chatMessage.getText());
                if(chatMessage.getDetails() != null && chatMessage.getDetails().size() > 0) {
                    vh.messageText.setText(chatMessage.getText());
                    vh.details.setVisibility(View.VISIBLE);

                    //Earned
                    vh.earned.setText(chatMessage.getDetails().get(0).getTitle());
                    vh.earnedValue.setText(String.valueOf(chatMessage.getDetails().get(0).getValue()));

                    //Total
                    vh.earnedTotal.setText(chatMessage.getDetails().get(1).getTitle());
                    vh.earnedTotalValue.setText(String.valueOf(chatMessage.getDetails().get(1).getValue()));
                }else{
                    vh.details.setVisibility(View.GONE);
                }
                break;
            }
            case TYPE_REMOTE_VIDEO: {
                final RemoteVideoViewHolder vh = (RemoteVideoViewHolder) holder;
                if (!chatMessage.hasBeenWatched()) {
                    vh.videoState.setImageResource(R.drawable.play);
                    vh.title.setText(chatMessage.getText());
                    vh.watched.setVisibility(View.GONE);
                    holder.itemView.setOnClickListener(new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            onClickSubject.onNext(holder.getAdapterPosition());
                        }
                    });
                } else {
                    holder.itemView.setOnClickListener(null);
                    vh.title.setText(chatMessage.getText());
                    vh.watched.setVisibility(View.VISIBLE);
                    vh.videoState.setImageResource(0);
                }

                break;
            }
            case TYPE_REMOTE_VERIFICATION: {
                final RemoteVerificationViewHolder vh = (RemoteVerificationViewHolder) holder;
                if(chatMessage.getAction().size() > 0 && chatMessage.getAction().get(0).getAction().equals("verify_phone_number")) {
                    String parsedString = MessageUtil.parseString(chatMessage.getText());
                    vh.message.setText(parsedString);
                    verifyButton = vh.verificationButton;

                    if(SharedPrefsUtil.isVerified()){
                        disableVerifyButton2(activity);
                    }

                    vh.verificationButton.setVisibility(View.VISIBLE);
                    vh.bind(verifyClicklistener);
                }else{
                    vh.message.setText(chatMessage.getText());
                    vh.verificationButton.setVisibility(View.GONE);
                }

                break;
            }
            case TYPE_DAY: {
                final DayViewHolder vh = (DayViewHolder) holder;
                String d = DateUtil.getDate("EEEE", new Date());
                vh.date.setText(d);
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

    public void disableVerifyButton2(Activity activity){
        verifyButton.setTextColor(Color.parseColor("#33565A64"));
        verifyButton.setBackground(ContextCompat.getDrawable(activity, R.drawable.disabled_background));
        verifyButton.setEnabled(false);
        verifyButton.setOnClickListener(null);
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

    public void clean(){
        activity = null;
        verifyButton = null;
    }
}
