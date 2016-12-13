package com.bakkenbaeck.token.manager;


import android.content.Context;
import android.content.SharedPreferences;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.network.rest.IdService;
import com.bakkenbaeck.token.network.rest.model.ServerTime;
import com.bakkenbaeck.token.network.rest.model.SignedUserDetails;
import com.bakkenbaeck.token.network.rest.model.UserDetails;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.BaseApplication;

import org.whispersystems.signalservice.internal.util.JsonUtil;

import java.util.concurrent.Callable;

import retrofit2.adapter.rxjava.HttpException;
import rx.Single;
import rx.SingleSubscriber;

public class UserManager {

    private final static String USER_ID = "uid";
    private final static String USER_NAME = "un";

    private User currentUser;
    private SharedPreferences prefs;
    private HDWallet wallet;


    public final Single<User> getObservable() {
        return Single.fromCallable(new Callable<User>() {
            @Override
            public User call() throws Exception {
                while(currentUser == null) {
                    Thread.sleep(100);
                }
                return currentUser;
            }
        });
    }

    public UserManager init(final HDWallet wallet) {
        this.wallet = wallet;
        initUser();
        return this;
    }

    private void initUser() {
        this.prefs = BaseApplication.get().getSharedPreferences(BaseApplication.get().getResources().getString(R.string.user_manager_pref_filename), Context.MODE_PRIVATE);
        if (!userExistsInPrefs()) {
            registerNewUser();
        } else {
            getExistingUser();
        }
    }

    private boolean userExistsInPrefs() {
        final String userId = this.prefs.getString(USER_ID, null);
        final String expectedAddress = wallet.getAddress();
        return userId != null && userId.equals(expectedAddress);
    }

    private void registerNewUser() {
        IdService.getApi().getTimestamp().subscribe(new SingleSubscriber<ServerTime>() {
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
        final String signature = this.wallet.signString(JsonUtil.toJson(ud));

        final SignedUserDetails sud = new SignedUserDetails()
                .setEthAddress(this.wallet.getAddress())
                .setUserDetails(ud)
                .setSignature(signature);

        IdService.getApi()
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
            if (((HttpException)error).code() == 400) {
                getExistingUser();
            }
        }
    };

    private void getExistingUser() {
        IdService.getApi()
                .getUser(this.wallet.getAddress())
                .subscribe(this.newUserSubscriber);
    }
}
