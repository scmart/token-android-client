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

package com.tokenbrowser.presenter;

import android.content.Intent;

import com.tokenbrowser.util.GcmUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.SignInActivity;
import com.tokenbrowser.view.activity.SignOutActivity;

import rx.Completable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SignOutPresenter implements Presenter<SignOutActivity> {

    private SignOutActivity activity;
    private Subscription signOutSubscription;

    @Override
    public void onViewAttached(SignOutActivity view) {
        this.activity = view;
        clearTasks();
    }

    private void clearTasks() {
        this.signOutSubscription =
                clearAndUnregister()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::goToSignInActivity,
                        __ -> goToSignInActivity());
    }

    private Completable clearAndUnregister() {
        return Completable
                .mergeDelayError(
                    unregisterChatGcm(),
                    unregisterEthGcm(),
                    clearUserDataAndLogOut()
        );
    }

    private Completable clearUserDataAndLogOut() {
        return BaseApplication
                .get()
                .getTokenManager()
                .clearUserData();
    }

    private Completable unregisterChatGcm() {
        return BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager()
                .tryUnregisterGcm();
    }

    private Completable unregisterEthGcm() {
        return GcmUtil
                .getGcmToken()
                .flatMap(token -> BaseApplication
                        .get()
                        .getTokenManager()
                        .getBalanceManager()
                        .unregisterFromGcm(token))
                .toCompletable();
    }

    private void goToSignInActivity() {
        final Intent intent = new Intent(this.activity, SignInActivity.class);
        this.activity.startActivity(intent);
        this.activity.finish();
    }

    @Override
    public void onViewDetached() {
        if (this.signOutSubscription != null) {
            this.signOutSubscription.unsubscribe();
        }
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.signOutSubscription = null;
        this.activity = null;
    }
}
