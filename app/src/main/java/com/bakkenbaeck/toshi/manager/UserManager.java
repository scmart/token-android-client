package com.bakkenbaeck.toshi.manager;


import android.content.SharedPreferences;

import com.bakkenbaeck.toshi.crypto.Wallet;
import com.bakkenbaeck.toshi.model.User;
import com.bakkenbaeck.toshi.network.rest.ToshiService;
import com.bakkenbaeck.toshi.util.LogUtil;
import com.bakkenbaeck.toshi.view.BaseApplication;
import com.securepreferences.SecurePreferences;

import org.mindrot.jbcrypt.BCrypt;

import rx.Observable;
import rx.Subscriber;
import rx.subjects.BehaviorSubject;

public class UserManager {

    private final String USER_ID = "u";
    private final String BCRYPT_SALT = "b";
    private final String AUTH_TOKEN = "t";
    private final String WALLET_PASSWORD = "w";

    private final BehaviorSubject<User> subject = BehaviorSubject.create();

    private User currentUser;
    private Wallet userWallet;
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
        this.userWallet = new Wallet();
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
            currentUser = userResponse;
            if (currentUser.isNewUser()) {
                storeReturnedUser(userResponse);
            } else {
                loadUserDetailsFromStorage();
            }
            initUserWallet();
            emitUser();
            userSubscriber.unsubscribe();
        }
    };

    private void initUserWallet() {
        final String walletPassword = this.prefs.getString(WALLET_PASSWORD, null);
        if (walletPassword == null) {
            throw new RuntimeException("Not yet implemented user inputted password");
        }

        this.userWallet.initWallet(walletPassword);
    }

    private void storeReturnedUser(final User userResponse) {
        prefs.edit()
                .putString(USER_ID, currentUser.getId())
                .putString(AUTH_TOKEN, userResponse.getAuthToken())
                .putString(BCRYPT_SALT, userResponse.getBcryptSalt())
                .putString(WALLET_PASSWORD, BCrypt.gensalt(16))
                .apply();
    }

    private void loadUserDetailsFromStorage() {
        final String authToken = this.prefs.getString(AUTH_TOKEN, null);
        final String bCryptSalt = this.prefs.getString(BCRYPT_SALT, null);
        this.currentUser.setAuthToken(authToken);
        this.currentUser.setBcryptSalt(bCryptSalt);
    }

    private void emitUser() {
        this.subject.onNext(this.currentUser);
    }
}
