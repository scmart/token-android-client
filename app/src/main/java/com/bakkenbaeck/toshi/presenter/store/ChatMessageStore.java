package com.bakkenbaeck.toshi.presenter.store;


import com.bakkenbaeck.toshi.model.ChatMessage;

import rx.subjects.AsyncSubject;
import rx.subjects.PublishSubject;

import static com.bakkenbaeck.toshi.model.ChatMessage.TYPE_REMOTE_VIDEO;

public class ChatMessageStore extends RealmStore<ChatMessage> {

    private final PublishSubject<Void> emptySetObservable = PublishSubject.create();
    private final PublishSubject<ChatMessage> newMessageObservable = PublishSubject.create();
    private final AsyncSubject<Boolean> unwatchedVideoObservable = AsyncSubject.create();
    private final PublishSubject<ChatMessage> newDateObservable = PublishSubject.create();

    public void load() {
        this.load(ChatMessage.class);
    }

    public void checkDate(){
        this.loadByDates(ChatMessage.class);
    }

    public PublishSubject<Void> getEmptySetObservable() {
        return this.emptySetObservable;
    }

    public PublishSubject<ChatMessage> getNewMessageObservable() {
        return this.newMessageObservable;
    }

    public AsyncSubject<Boolean> getUnwatchedVideoObservable() {
        return this.unwatchedVideoObservable;
    }

    public PublishSubject<ChatMessage> getNewDateObservable(){
        return this.newDateObservable;
    }

    @Override
    void onNewDate(ChatMessage message) {
        this.newDateObservable.onNext(message);
    }

    @Override
    void onNewObject(final ChatMessage newMessage) {
        this.newMessageObservable.onNext(newMessage);
        if (newMessage.getType() == TYPE_REMOTE_VIDEO) {
            this.unwatchedVideoObservable.onNext(!newMessage.hasBeenWatched());
        }
    }

    @Override
    void onEmptySetAfterLoad() {
        this.emptySetObservable.onCompleted();
    }

    @Override
    void onFinishedLoading() {
        this.unwatchedVideoObservable.onCompleted();
    }
}
