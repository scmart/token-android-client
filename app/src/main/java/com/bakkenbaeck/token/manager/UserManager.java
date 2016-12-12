package com.bakkenbaeck.token.manager;


import android.content.Context;
import android.content.SharedPreferences;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.network.rest.TokenService;
import com.bakkenbaeck.token.network.rest.model.ServerTime;
import com.bakkenbaeck.token.network.rest.model.SignedUserDetails;
import com.bakkenbaeck.token.network.rest.model.UserDetails;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.BaseApplication;

import org.whispersystems.signalservice.internal.util.JsonUtil;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.subjects.BehaviorSubject;

public class UserManager {

    private final BehaviorSubject<User> subject = BehaviorSubject.create();
    private final static String USER_ID = "uid";
    private final static String USER_NAME = "un";

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
                userHDWallet = new HDWallet();
                initUser();
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
            registerNewUser();
        }
    }

    private boolean userExistsInPrefs() {
        final String userId = this.prefs.getString(USER_ID, null);
        return userId != null && userId.equals(this.getWalletAddress());
    }

    private void registerNewUser() {
        TokenService.getApi().getTimestamp().subscribe(new SingleSubscriber<ServerTime>() {
            @Override
            public void onSuccess(final ServerTime serverTime) {
                final long timestamp = serverTime.get();
                registerNewUserWithTimestamp(timestamp);
            }

            @Override
            public void onError(final Throwable error) {
                LogUtil.e(getClass(), error.toString());
            }
        });

    }

    private void registerNewUserWithTimestamp(final long timestamp) {
        final UserDetails ud = new UserDetails().setTimestamp(timestamp);
        final String signature = userHDWallet.signString(JsonUtil.toJson(ud));

        final SignedUserDetails sud = new SignedUserDetails()
                .setEthAddress(userHDWallet.getAddressAsHex())
                .setUserDetails(ud)
                .setSignature(signature);

        TokenService.getApi()
                .registerUser(sud)
                .subscribe(newUserSubscriber);
    }

    private final SingleSubscriber<User> newUserSubscriber = new SingleSubscriber<User>() {

        @Override
        public void onSuccess(final User user) {
            currentUser = user;
            storeReturnedUser(user);
        }

        private void storeReturnedUser(final User user) {
            prefs.edit()
                    .putString(USER_ID, user.getOwnerAddress())
                    .putString(USER_NAME, user.getUsername())
                    .apply();
        }

        @Override
        public void onError(final Throwable error) {
            LogUtil.error(getClass(), error.toString());
        }
    };

    public String getWalletAddress(){
        return userHDWallet.getAddressAsHex();
    }

    public HDWallet getWallet() {
        return this.userHDWallet;
    }
}
