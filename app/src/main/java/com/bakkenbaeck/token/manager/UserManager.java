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
import com.bakkenbaeck.token.presenter.store.ContactStore;
import com.bakkenbaeck.token.util.LogUtil;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;

import org.whispersystems.signalservice.internal.util.JsonUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.adapter.rxjava.HttpException;
import rx.SingleSubscriber;
import rx.subjects.BehaviorSubject;

public class UserManager {

    private final static String USER_ID = "uid";
    private final static String USER_NAME = "un";

    private final BehaviorSubject<User> userSubject = BehaviorSubject.create();
    private SharedPreferences prefs;
    private HDWallet wallet;
    private ExecutorService dbThreadExecutor;
    private ContactStore contactStore;

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
                UserManager.this.contactStore = new ContactStore();
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
        final String expectedAddress = wallet.getAddress();
        return userId != null && userId.equals(expectedAddress);
    }

    private void registerNewUser() {
        IdService.getApi().getTimestamp().subscribe(new SingleSubscriber<ServerTime>() {
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
                .putString(USER_ID, user.getAddress())
                .putString(USER_NAME, user.getUsername())
                .apply();
        this.userSubject.onNext(user);
    }

    private void getExistingUser() {
        IdService.getApi()
                .getUser(this.wallet.getAddress())
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
                userDetails.setTimestamp(timestamp);
                updateUserWithTimestamp(userDetails, completionCallback);
                this.unsubscribe();
            }

            @Override
            public void onError(final Throwable error) {
                LogUtil.e(getClass(), error.toString());
                this.unsubscribe();
                completionCallback.onError(error);
            }
        });
    }

    private void updateUserWithTimestamp(final UserDetails userDetails, final SingleSubscriber<Void> completionCallback) {
        final String signature = this.wallet.signString(JsonUtil.toJson(userDetails));

        final SignedUserDetails sud = new SignedUserDetails()
                .setEthAddress(this.wallet.getAddress())
                .setUserDetails(userDetails)
                .setSignature(signature);

        IdService.getApi()
                .updateUser(this.wallet.getAddress(), sud)
                .subscribe(new SingleSubscriber<User>() {
                    @Override
                    public void onSuccess(final User user) {
                        updateCurrentUser(user);
                        completionCallback.onSuccess(null);
                    }

                    @Override
                    public void onError(final Throwable error) {
                        LogUtil.error(getClass(), error.toString());
                        this.unsubscribe();
                        completionCallback.onError(error);
                    }
                });
    }

    // If the user at the address is already saved as a contact this method does nothing.
    // If the user is not a contact, they will be added.
    public void tryAddContact(final String contactAddress) {
        this.dbThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                contactStore
                        .load(contactAddress)
                        .subscribe(this.handleContactLookup);
            }

            private final SingleSubscriber<User> handleContactLookup = new SingleSuccessSubscriber<User>() {
                @Override
                public void onSuccess(final User user) {
                    if (user != null) return;
                    fetchAndSaveContact();
                }
            };

            private void fetchAndSaveContact() {
                IdService.getApi()
                        .getUser(contactAddress)
                        .subscribe(this.handleUserFetched);
            }

            private final SingleSubscriber<User> handleUserFetched = new SingleSuccessSubscriber<User>() {
                @Override
                public void onSuccess(final User user) {
                    dbThreadExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            contactStore.save(user);
                        }
                    });
                }
            };
        });
    }
}
