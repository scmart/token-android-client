package com.bakkenbaeck.token.manager;


import android.content.SharedPreferences;
import android.util.Log;

import com.bakkenbaeck.token.crypto.Wallet;
import com.bakkenbaeck.token.model.CryptoDetails;
import com.bakkenbaeck.token.model.User;
import com.bakkenbaeck.token.network.rest.TokenService;
import com.bakkenbaeck.token.util.OnCompletedObserver;
import com.bakkenbaeck.token.util.OnNextObserver;
import com.bakkenbaeck.token.util.RetryWithBackoff;
import com.bakkenbaeck.token.view.BaseApplication;
import com.securepreferences.SecurePreferences;

import org.mindrot.jbcrypt.BCrypt;

import rx.Observable;
import rx.Observer;
import rx.subjects.BehaviorSubject;

public class UserManager {

    private final static String BCRYPT_SALT = "b";
    private final static String USER_ID = "u";
    private final static String AUTH_TOKEN = "t";
    private final static String WALLET_PASSWORD = "w";
    private final static String HAVE_REGISTERED = "r";

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

    public String signTransaction(final String transaction) {
        return this.userWallet.sign(transaction);
    }

    public void refresh() {
        userExistsInPrefs();
    }

    private void initUser() {
        this.prefs = new SecurePreferences(BaseApplication.get(), "", "um");
        this.userWallet = new Wallet();
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
            initUserWallet().subscribe(walletCreatedSubscriber);
            emitUser();
        }

        private void storeReturnedUser(final User userResponse) {
            prefs.edit()
                    .putString(USER_ID, currentUser.getId())
                    .putString(AUTH_TOKEN, userResponse.getAuthToken())
                    .putString(BCRYPT_SALT, userResponse.getBcryptSalt())
                    .putString(WALLET_PASSWORD, BCrypt.gensalt(16))
                    .apply();
        }
    };

    private final OnNextObserver<User> existingUserSubscriber = new OnNextObserver<User>() {
        @Override
        public void onNext(final User userResponse) {
            currentUser = userResponse;
            loadUserDetailsFromStorage();
            initUserWallet().subscribe(walletCreatedSubscriber);
            emitUser();
        }

        private void loadUserDetailsFromStorage() {
            final String authToken = prefs.getString(AUTH_TOKEN, null);
            final String bCryptSalt = prefs.getString(BCRYPT_SALT, null);
            currentUser.setAuthToken(authToken);
            currentUser.setBcryptSalt(bCryptSalt);
        }
    };

    private Observer<Wallet> walletCreatedSubscriber = new OnNextObserver<Wallet>() {
        @Override
        public void onNext(final Wallet wallet) {
            final boolean hasRegisteredWithBackend = prefs.getBoolean(HAVE_REGISTERED, false);
            if (hasRegisteredWithBackend) {
                return;
            }

            final CryptoDetails cryptoDetails = new CryptoDetails()
                    .setAesEncodedPrivateKey(wallet.getEncryptedPrivateKey())
                    .setBCryptedPassword(wallet.getBCryptedPassword())
                    .setEthAddress(wallet.getAddress());
            TokenService.getApi().putUserCryptoDetails(
                        currentUser.getAuthToken(),
                        currentUser.getId(),
                        cryptoDetails)
                    .retryWhen(new RetryWithBackoff())
                    .subscribe(storedCryptoSubscriber);
        }
    };

    private OnCompletedObserver storedCryptoSubscriber = new OnCompletedObserver() {
        @Override
        public void onCompleted() {
            prefs.edit().putBoolean(HAVE_REGISTERED, true).apply();
        }
    };

    private Observable<Wallet> initUserWallet() {
        final String walletPassword = this.prefs.getString(WALLET_PASSWORD, null);
        final String salt = prefs.getString(BCRYPT_SALT, null);
        if (walletPassword == null) {
            throw new RuntimeException("Not yet implemented user inputted password");
        }

        return this.userWallet.initWallet(walletPassword, salt);
    }

    private void emitUser() {
        this.subject.onNext(this.currentUser);
    }
}
