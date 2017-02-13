package com.bakkenbaeck.token.presenter.store;


import com.bakkenbaeck.token.model.local.Conversation;
import com.bakkenbaeck.token.model.local.User;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Single;

public class ConversationStore {

    public void saveNew(final User user) {
        final Realm realm = Realm.getDefaultInstance();

        final Conversation conversation = new Conversation(user);
        realm.beginTransaction();
        realm.insert(conversation);
        realm.commitTransaction();
        realm.close();
    }

    public Single<RealmResults<Conversation>> loadAll() {
        return Single.fromCallable(() -> {
            final Realm realm = Realm.getDefaultInstance();
            final RealmQuery<Conversation> query = realm.where(Conversation.class);
            final RealmResults<Conversation> results = query.findAll();
            realm.close();
            return results;
        });
    }

}
