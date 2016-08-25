package com.bakkenbaeck.toshi.manager;


import android.content.SharedPreferences;

import com.bakkenbaeck.toshi.http.ToshiService;
import com.bakkenbaeck.toshi.model.User;
import com.bakkenbaeck.toshi.util.LogUtil;
import com.bakkenbaeck.toshi.view.BaseApplication;
import com.securepreferences.SecurePreferences;

import rx.Observable;
import rx.Subscriber;
import rx.subjects.ReplaySubject;

public class UserManager {

    private final String USER_ID = "user_id";
    private final ReplaySubject<User> subject = ReplaySubject.create();

    private User currentUser;
    private SharedPreferences prefs;

    public final Observable<User> getObservable() {
        return this.subject.asObservable();
    }

    public UserManager init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initUser();
            }
        }).start();

        return this;
    }

    private void initUser() {
        this.prefs = new SecurePreferences(BaseApplication.get());
        if (!userExistsInPrefs()) {
            requestNewUser();
        }
    }

    private boolean userExistsInPrefs() {
        final String userId = this.prefs.getString(USER_ID, null);
        if (userId == null) {
            return false;
        }

        getExistingUser(userId);
        return true;
    }

    private void requestNewUser() {
        final Observable<User> call = ToshiService.getApi().requestUserId();
        call.subscribe(this.userSubscriber);
    }

    private void getExistingUser(final String userId) {
        final Observable<User> call = ToshiService.getApi().getUser(userId);
        call.subscribe(this.userSubscriber);
    }

    private final Subscriber<User> userSubscriber = new Subscriber<User>() {
        @Override
        public void onCompleted() {}

        @Override
        public void onError(final Throwable e) {
            LogUtil.e(getClass(), e.toString());
        }

        @Override
        public void onNext(final User userResponse) {
            storeAndEmitReturnedUser(userResponse);
            userSubscriber.unsubscribe();
        }
    };

    private void storeAndEmitReturnedUser(final User userResponse) {
        currentUser = userResponse;
        prefs.edit().putString(USER_ID, currentUser.getId()).apply();
        emitUser();
    }

    private void emitUser() {
        this.subject.onNext(this.currentUser);
    }
}
