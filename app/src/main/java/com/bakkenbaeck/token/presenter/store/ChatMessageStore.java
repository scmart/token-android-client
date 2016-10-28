package com.bakkenbaeck.token.presenter.store;


import com.bakkenbaeck.token.model.ChatMessage;

import rx.subjects.AsyncSubject;
import rx.subjects.PublishSubject;

import static com.bakkenbaeck.token.model.ChatMessage.TYPE_REMOTE_VIDEO;

public class ChatMessageStore extends RealmStore<ChatMessage> {

    private final PublishSubject<Void> emptySetObservable = PublishSubject.create();
    private final PublishSubject<ChatMessage> newMessageObservable = PublishSubject.create();
    private final AsyncSubject<Boolean> unwatchedVideoObservable = AsyncSubject.create();

    public void load() {
        this.load(ChatMessage.class);
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
