package com.bakkenbaeck.token.manager;


import android.content.Context;
import android.content.SharedPreferences;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.model.network.ServerTime;
import com.bakkenbaeck.token.model.network.UserDetails;
import com.bakkenbaeck.token.network.IdService;
import com.bakkenbaeck.token.presenter.store.UserStore;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.view.BaseApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.adapter.rxjava.HttpException;
import rx.SingleSubscriber;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class UserManager {

    private final static String USER_ID = "uid";
    private final static String USER_NAME = "un";

    private final BehaviorSubject<User> userSubject = BehaviorSubject.create();
    private SharedPreferences prefs;
    private HDWallet wallet;
    private ExecutorService dbThreadExecutor;
    private UserStore userStore;

    public final BehaviorSubject<User> getUserObservable() {
        return this.userSubject;
    }

    public UserManager init(final HDWallet wallet) {
        this.wallet = wallet;
        initDatabase();
        initUser();
        return this;
    }

    private void initDatabase() {
        this.dbThreadExecutor = Executors.newSingleThreadExecutor();
        this.dbThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                UserManager.this.userStore = new UserStore();
            }
        });
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
        final String expectedAddress = wallet.getOwnerAddress();
        return userId != null && userId.equals(expectedAddress);
    }

    private void registerNewUser() {
        IdService.getApi()
        .getTimestamp()
        .subscribe(new SingleSubscriber<ServerTime>() {
            @Override
            public void onSuccess(final ServerTime serverTime) {
                final long timestamp = serverTime.get();
                registerNewUserWithTimestamp(timestamp);
                this.unsubscribe();
            }

            @Override
            public void onError(final Throwable error) {
                LogUtil.e(getClass(), error.toString());
                this.unsubscribe();
            }
        });
    }

    private void registerNewUserWithTimestamp(final long timestamp) {
        final UserDetails ud = new UserDetails()
                .setWalletAddress(this.wallet.getPaymentAddress());

        IdService.getApi()
                .registerUser(ud, timestamp)
                .subscribe(newUserSubscriber);
    }

    private final SingleSubscriber<User> newUserSubscriber = new SingleSubscriber<User>() {

        @Override
        public void onSuccess(final User user) {
            updateCurrentUser(user);
        }

        @Override
        public void onError(final Throwable error) {
            LogUtil.error(getClass(), error.toString());
            if (error instanceof HttpException && ((HttpException)error).code() == 400) {
                getExistingUser();
            }
        }
    };

    private void updateCurrentUser(final User user) {
        prefs.edit()
                .putString(USER_ID, user.getOwnerAddress())
                .putString(USER_NAME, user.getUsername())
                .apply();
        this.userSubject.onNext(user);
    }

    private void getExistingUser() {
        IdService.getApi()
                .getUser(this.wallet.getOwnerAddress())
                .subscribe(this.newUserSubscriber);
    }

    public void updateUser(final UserDetails userDetails, final SingleSubscriber<Void> completionCallback) {
        IdService
            .getApi()
            .getTimestamp()
            .subscribe(new SingleSubscriber<ServerTime>() {
                @Override
                public void onSuccess(final ServerTime serverTime) {
                    final long timestamp = serverTime.get();
                    updateUserWithTimestamp(userDetails, timestamp, completionCallback);
                    this.unsubscribe();
                }

                @Override
                public void onError(final Throwable error) {
                    this.unsubscribe();
                    completionCallback.onError(error);
                }
            });
    }

    private void updateUserWithTimestamp(
            final UserDetails userDetails,
            final long timestamp,
            final SingleSubscriber<Void> completionCallback) {

        IdService.getApi()
                .updateUser(this.wallet.getOwnerAddress(), userDetails, timestamp)
                .subscribe(new SingleSubscriber<User>() {
                    @Override
                    public void onSuccess(final User user) {
                        updateCurrentUser(user);
                        completionCallback.onSuccess(null);
                    }

                    @Override
                    public void onError(final Throwable error) {
                        this.unsubscribe();
                        completionCallback.onError(error);
                    }
                });
    }

    public void getUserFromAddress(final String contactAddress, final SingleSubscriber<User> callback) {

        this.userStore
                .loadForAddress(contactAddress)
                .subscribeOn(Schedulers.from(this.dbThreadExecutor))
                .observeOn(Schedulers.from(this.dbThreadExecutor))
                .subscribe((user) -> handleContactLookup(user, contactAddress, callback));
    }

    private void handleContactLookup(final User user, final String contactAddress, final SingleSubscriber<User> callback) {
        if (user != null) {
            callback.onSuccess(user);
            return;
        }

        fetchContact(contactAddress, callback);
    }

    private void fetchContact(final String contactAddress, final SingleSubscriber<User> callback) {
        IdService
                .getApi()
                .getUser(contactAddress)
                .subscribe(callback);
    }

}
