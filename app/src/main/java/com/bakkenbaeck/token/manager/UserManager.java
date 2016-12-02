package com.bakkenbaeck.token.manager;


import android.content.Context;
import android.content.SharedPreferences;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.network.rest.TokenService;
import com.bakkenbaeck.token.util.OnNextObserver;
import com.bakkenbaeck.token.util.RetryWithBackoff;
import com.bakkenbaeck.token.view.BaseApplication;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Single;
import rx.subjects.BehaviorSubject;

public class UserManager {

    private final static String USER_ID = "u";
    private final static String AUTH_TOKEN = "t";

    private final BehaviorSubject<User> subject = BehaviorSubject.create();

    private User currentUser;
    private HDWallet userHDWallet;
    private SharedPreferences prefs;

    public final Observable<User> getObservable() {
        return this.subject.asObservable();
    }

    public Single<UserManager> init() {
        return Single.fromCallable(new Callable<UserManager>() {
            @Override
            public UserManager call() throws Exception {
                initUser();
                userHDWallet = new HDWallet();
                return UserManager.this;
            }
        });
    }

    public String signTransaction(final String transaction) {
        return this.userHDWallet.signHexString(transaction);
    }

    public void refresh() {
        userExistsInPrefs();
    }

    private void initUser() {
        this.prefs = BaseApplication.get().getSharedPreferences(BaseApplication.get().getResources().getString(R.string.user_manager_pref_filename), Context.MODE_PRIVATE);
        if (!userExistsInPrefs()) {
            requestNewUser();
        }
    }

    private boolean userExistsInPrefs() {
        final String userId = this.prefs.getString(USER_ID, null);
        final String authToken = this.prefs.getString(AUTH_TOKEN, null);
        if (userId == null || authToken == null) {
            return false;
        }

        getExistingUser(authToken, userId);
        return true;
    }

    private void requestNewUser() {
        final Observable<User> call = TokenService.getApi().requestUserId();
        call.retryWhen(new RetryWithBackoff())
            .subscribe(this.newUserSubscriber);
    }

    private void getExistingUser(final String authToken, final String userId) {
        final Observable<User> call = TokenService.getApi().getUser(authToken, userId);
        call.retryWhen(new RetryWithBackoff())
            .subscribe(this.existingUserSubscriber);
    }

    private final OnNextObserver<User> newUserSubscriber = new OnNextObserver<User>() {

        @Override
        public void onNext(final User userResponse) {
            currentUser = userResponse;
            storeReturnedUser(userResponse);
            emitUser();
        }

        private void storeReturnedUser(final User userResponse) {
            prefs.edit()
                    .putString(USER_ID, currentUser.getId())
                    .putString(AUTH_TOKEN, userResponse.getAuthToken())
                    .apply();
        }
    };

    private final OnNextObserver<User> existingUserSubscriber = new OnNextObserver<User>() {
        @Override
        public void onNext(final User userResponse) {
            currentUser = userResponse;
            loadUserDetailsFromStorage();
            emitUser();
        }

        private void loadUserDetailsFromStorage() {
            final String authToken = prefs.getString(AUTH_TOKEN, null);
            currentUser.setAuthToken(authToken);
        }
    };

    private void emitUser() {
        this.subject.onNext(this.currentUser);
    }

    public String getWalletAddress(){
        return userHDWallet.getAddress();
    }

    public HDWallet getWallet() {
        return this.userHDWallet;
    }
}
