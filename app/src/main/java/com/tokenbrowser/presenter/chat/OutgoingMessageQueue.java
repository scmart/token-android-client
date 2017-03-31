package com.tokenbrowser.presenter.chat;


import com.tokenbrowser.model.local.SofaMessage;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.BaseApplication;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * A pipeline for sending sofa messages to a remote user.
 * It will take care of queuing up and sending messages if messages are sent before
 * the queue has been initialised.
 * It will suppress attempts to double subscribe; and will handle swapping the user
 * in the middle of its lifetime.
 * Example usage:
 * <pre> {@code

OutgoingMessageQueue queue = new OutgoingMessageQueue();
queue.send(sofaMessage); // This message will be sent after initialisation
queue.init(user); // Let the queue know who these messages should be sent to
queue.send(sofaMessage); // This message will be sent immediately.
queue.clear(); // Cleans up all state, and unsubscribes everything.
} </pre>
 */
/* package */ class OutgoingMessageQueue {

    private final PublishSubject<SofaMessage> messagesReadyForSending;
    private final List<SofaMessage> preInitMessagesQueue;
    private final CompositeSubscription subscriptions;
    private User remoteUser;

    /**
     * Constructs OutgoingMessageQueue.
     * <p>
     * Nothing will be sent until {@link #init(User)} has been called, but it is possible
     * to queue messages already via {@link #send(SofaMessage)}.
     *
     * @return the constructed OutgoingMessageQueue
     */
    /* package */ OutgoingMessageQueue() {
        this.messagesReadyForSending = PublishSubject.create();
        this.preInitMessagesQueue = new ArrayList<>();
        this.subscriptions = new CompositeSubscription();
    }

    /**
     * Sends or queues a message that may eventually be sent to a remote user
     * <p>
     * If {@link #init(User)} has already been called then the message will be sent to the remote user
     * immediately.
     * If {@link #init(User)} has not been called then the message will be queued until {@link #init(User)}
     * is called.
     *
     * @param message
     *              The message to be sent.
     */
    /* package */ void send(final SofaMessage message) {
        // If we already know who to send the message to; send it.
        // If not, queue it until we know where to send the message.
        if (this.remoteUser != null) {
            this.messagesReadyForSending.onNext(message);
        } else {
            this.preInitMessagesQueue.add(message);
        }
    }

    /**
     * Clear all the state; stop processing messages.
     * <p>
     * Any messages not yet sent will be lost. It is wise to call this method when possible to
     * clear any state, and release memory.
     */
    /* package */ void clear() {
        this.subscriptions.clear();
        this.remoteUser = null;
    }

    /**
     * Initialises OutgoingMessageQueue with the user who will receive messages
     * <p>
     * Any messages that have already been queued by previous calls to {@link #send(SofaMessage)}
     * will be processed in the order they were sent. Any new calls to {@link #send(SofaMessage)}
     * will send to the remote user immediately (though after the queue has been processed)
     *
     * If this method is called multiple times it will not result in several subscriptions.
     * If the method is called a second time with the same User then nothing changes.
     * If the method is called a second time with a different user then messages will no longer be
     * sent to the first user; all messages will be routed to the second user.
     *
     * @param remoteUser
     *              The user who the messges will be sent to.
     */
    /* package */ void init(final User remoteUser) {
        if (remoteUser == this.remoteUser) {
            LogUtil.print(getClass(), "Suppressing a double subscription");
            return;
        }

        if (this.remoteUser != null) {
            LogUtil.print(getClass(), "Subscribing to a different user, so clearing previous subscriptions. Was this intentional?");
            this.clear();
        }

        this.remoteUser = remoteUser;
        attachMessagesReadyForSendingSubscriber();
        processPreInitMessagesQueue();
    }

    private void attachMessagesReadyForSendingSubscriber() {
        final Subscription subscription =
                this.messagesReadyForSending
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .subscribe(outgoingSofaMessage ->
                                BaseApplication
                                        .get()
                                        .getTokenManager()
                                        .getSofaMessageManager()
                                        .sendAndSaveMessage(this.remoteUser, outgoingSofaMessage)
                        );
        this.subscriptions.add(subscription);
    }

    private void processPreInitMessagesQueue() {
        final Subscription subscription =
                Observable
                        .from(this.preInitMessagesQueue)
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.io())
                        .doOnCompleted(this.preInitMessagesQueue::clear)
                        .subscribe(this.messagesReadyForSending::onNext);
        this.subscriptions.add(subscription);
    }
}
