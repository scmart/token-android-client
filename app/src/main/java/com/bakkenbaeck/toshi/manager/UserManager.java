package com.bakkenbaeck.toshi.manager;


import android.content.SharedPreferences;

import com.bakkenbaeck.toshi.model.User;
import com.bakkenbaeck.toshi.network.rest.ToshiService;
import com.bakkenbaeck.toshi.util.LogUtil;
import com.bakkenbaeck.toshi.view.BaseApplication;
import com.securepreferences.SecurePreferences;

import rx.Observable;
import rx.Subscriber;
import rx.subjects.BehaviorSubject;

public class UserManager {

    private final String USER_ID = "u";
    private final String BCRYPT_SALT = "b";
    private final String AUTH_TOKEN = "t";

    private final BehaviorSubject<User> subject = BehaviorSubject.create();

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

        // If this response contains an auth token then we are creating a new user
        // In which case save everything to preferences for later use.
        if (userResponse.getAuthToken() != null) {
            prefs.edit()
                    .putString(USER_ID, currentUser.getId())
                    .putString(AUTH_TOKEN, userResponse.getAuthToken())
                    .putString(BCRYPT_SALT, userResponse.getBcryptSalt())
                    .apply();
        }
        emitUser();
    }

    private void emitUser() {
        this.subject.onNext(this.currentUser);
    }
}
