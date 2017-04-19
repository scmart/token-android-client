/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.manager;


import android.content.Context;
import android.content.SharedPreferences;

import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.manager.network.IdService;
import com.tokenbrowser.manager.store.ContactStore;
import com.tokenbrowser.manager.store.UserStore;
import com.tokenbrowser.model.local.Contact;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.network.ServerTime;
import com.tokenbrowser.model.network.UserDetails;
import com.tokenbrowser.model.network.UserSearchResults;
import com.tokenbrowser.util.FileNames;
import com.tokenbrowser.util.FileUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.BaseApplication;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class UserManager {

    private final static String USER_ID = "uid";
    private final static String FORM_DATA_NAME = "Profile-Image-Upload";

    private final BehaviorSubject<User> userSubject = BehaviorSubject.create();
    private SharedPreferences prefs;
    private HDWallet wallet;
    private ContactStore contactStore;
    private UserStore userStore;

    /* package */ UserManager() {
        initDatabases();
    }

    public final BehaviorSubject<User> getUserObservable() {
        return this.userSubject;
    }

    public final Single<User> getCurrentUser() {
        return
                this.userSubject
                .filter(user -> user != null)
                .first()
                .toSingle();
    }

    public UserManager init(final HDWallet wallet) {
        this.wallet = wallet;
        this.prefs = BaseApplication.get().getSharedPreferences(FileNames.USER_PREFS, Context.MODE_PRIVATE);
        attachConnectivityListener();
        return this;
    }

    private void initDatabases() {
        this.contactStore = new ContactStore();
        this.userStore = new UserStore();
    }

    private void attachConnectivityListener() {
        // Whenever the network changes init the user.
        // This is dumb and potentially inefficient but it shouldn't have
        // any adverse effects and it is easy to improve later.
        BaseApplication
                .get()
                .isConnectedSubject()
                .subscribe(isConnected -> initUser());
    }

    private void initUser() {
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
        IdService
            .getApi()
            .getTimestamp()
            .subscribe(this::registerNewUserWithTimestamp, this::handleError);
    }

    private void handleError(final Throwable throwable) {
        LogUtil.e(getClass(), "Unable to register/fetch user: " + throwable.toString());
    }

    private void registerNewUserWithTimestamp(final ServerTime serverTime) {
        final UserDetails ud = new UserDetails().setPaymentAddress(this.wallet.getPaymentAddress());

        IdService
                .getApi()
                .registerUser(ud, serverTime.get())
                .subscribe(this::updateCurrentUser, this::handleUserRegistrationFailed);
    }

    private void handleUserRegistrationFailed(final Throwable throwable) {
        LogUtil.error(getClass(), throwable.toString());
        if (throwable instanceof HttpException && ((HttpException)throwable).code() == 400) {
            getExistingUser();
        }
    }

    private void getExistingUser() {
        IdService.getApi()
                .getUser(this.wallet.getOwnerAddress())
                .subscribe(this::updateCurrentUser, this::handleError);
    }

    private void updateCurrentUser(final User user) {
        prefs
            .edit()
            .putString(USER_ID, user.getTokenId())
            .apply();
        this.userSubject.onNext(user);
    }

    public Single<User> updateUser(final UserDetails userDetails) {
        return IdService
                .getApi()
                .getTimestamp()
                .subscribeOn(Schedulers.io())
                .flatMap(serverTime -> updateUserWithTimestamp(userDetails, serverTime));
    }

    private Single<User> updateUserWithTimestamp(
            final UserDetails userDetails,
            final ServerTime serverTime) {

        return IdService.getApi()
                .updateUser(this.wallet.getOwnerAddress(), userDetails, serverTime.get())
                .subscribeOn(Schedulers.io())
                .doOnSuccess(this::updateCurrentUser);
    }

    public Observable<User> getUserFromAddress(final String userAddress) {
        return Observable
                .concat(
                        this.userStore.loadForAddress(userAddress),
                        this.fetchAndCacheFromNetworkByTokenId(userAddress))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .first(user -> user != null && !user.needsRefresh());
    }

    public Single<User> getUserFromPaymentAddress(final String paymentAddress) {
        return Single
                .concat(
                        Single.just(userStore.loadForPaymentAddress(paymentAddress)),
                        this.fetchAndCacheFromNetworkByPaymentAddress(paymentAddress).toSingle()
                )
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .first(user -> user != null && !user.needsRefresh())
                .toSingle();

    }

    private Observable<User> fetchAndCacheFromNetworkByTokenId(final String userAddress) {
        return IdService
                .getApi()
                .getUser(userAddress)
                .toObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(this::cacheUser);
    }

    private Observable<User> fetchAndCacheFromNetworkByPaymentAddress(final String paymentAddress) {
        return IdService
                .getApi()
                .searchByPaymentAddress(paymentAddress)
                .toObservable()
                .filter(userSearchResults -> userSearchResults.getResults().size() > 0)
                .map(userSearchResults -> userSearchResults.getResults().get(0))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnNext(this::cacheUser);
    }

    private void cacheUser(final User user) {
        if (this.userStore == null) {
            return;
        }

        this.userStore.save(user);
    }

    public Single<List<Contact>> loadAllContacts() {
        return this.contactStore
                .loadAll()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }

    public Single<List<User>> searchOfflineUsers(final String query) {
        return this.userStore
                .queryUsername(query)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }

    public Single<List<User>> searchOnlineUsers(final String query) {
        return IdService
                .getApi()
                .searchByUsername(query)
                .subscribeOn(Schedulers.io())
                .map(UserSearchResults::getResults);
    }

    public Single<User> getUserByUsername(final String username) {
        return Single
                .concat(
                        searchOfflineUsers(username),
                        searchOnlineUsers(username)
                )
                .subscribeOn(Schedulers.io())
                .flatMapIterable(users -> users)
                .filter(user -> user != null && user.getUsernameForEditing().equals(username))
                .first(user -> !user.needsRefresh())
                .toSingle();
    }

    public Single<Void> webLogin(final String loginToken) {
        return IdService
            .getApi()
            .getTimestamp()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .flatMap((serverTime) -> webLoginWithTimestamp(loginToken, serverTime));
    }

    private Single<Void> webLoginWithTimestamp(final String loginToken, final ServerTime serverTime) {
        if (serverTime == null) {
            throw new IllegalStateException("ServerTime was null");
        }

        return IdService
                .getApi()
                .webLogin(loginToken, serverTime.get());
    }

    public Single<Boolean> isUserAContact(final User user) {
        return Single
                .fromCallable(() -> contactStore.userIsAContact(user))
                .subscribeOn(Schedulers.io());
    }

    public void deleteContact(final User user) {
        this.contactStore.delete(user);
    }

    public void saveContact(final User user) {
        this.contactStore.save(user);
    }

    public Single<ServerTime> getTimestamp() {
        return IdService
                .getApi()
                .getTimestamp();
    }

    public Single<User> uploadAvatar(final File file) {
        final FileUtil fileUtil = new FileUtil();
        final String mimeType = fileUtil.getMimeTypeFromFilename(file.getName());
        if (mimeType == null) {
            return Single.error(new IllegalArgumentException("Unable to determine file type from file."));
        }
        final MediaType mediaType = MediaType.parse(mimeType);
        final RequestBody requestFile = RequestBody.create(mediaType, file);
        final MultipartBody.Part body = MultipartBody.Part.createFormData(FORM_DATA_NAME, file.getName(), requestFile);

        return IdService
                .getApi()
                .getTimestamp()
                .subscribeOn(Schedulers.io())
                .flatMap(serverTime -> IdService
                        .getApi()
                        .uploadFile(body, serverTime.get()))
                .doOnSuccess(this.userSubject::onNext);
    }

    public void clear() {
        clearCache();
        clearUserId();
    }

    private void clearUserId() {
        this.prefs
                .edit()
                .putString(USER_ID, null)
                .apply();
        this.userSubject.onNext(null);
    }

    private void clearCache() {
        try {
            IdService
                    .get()
                    .clearCache();
        } catch (IOException e) {
            LogUtil.e(getClass(), e.getMessage());
        }
    }
}
