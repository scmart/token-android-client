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

package com.tokenbrowser.view.activity;


import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.tokenbrowser.presenter.Presenter;
import com.tokenbrowser.R;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.custom.OfflineViewRenderer;

import rx.Subscription;

public abstract class OfflineViewBasePresenterActivity<P extends Presenter<V>, V extends OfflineViewRenderer> extends BasePresenterActivity<P, V> {

    private Subscription connectionSubscription;
    private Snackbar offlineSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        attachConnectionObserver();
    }

    private void attachConnectionObserver() {
        this.connectionSubscription =
                BaseApplication
                .get()
                .isConnectedSubject()
                .subscribe(this::handleConnectionChange);
    }

    private void handleConnectionChange(final Boolean isConnected) {
        if (isConnected && this.offlineSnackbar == null) {
            return;
        }

        if (isConnected) {
            this.offlineSnackbar.dismiss();
        } else {
            createSnackbar();
            this.offlineSnackbar.show();
        }
    }

    private void createSnackbar() {
        try {
            this.offlineSnackbar = Snackbar.make(
                    getPresenterView().getOfflineViewContainer(),
                    BaseApplication.get().getText(R.string.error__offline),
                    Snackbar.LENGTH_INDEFINITE);

            // Workaround to get white text on certain phones that override defaults
            final View view = this.offlineSnackbar.getView();
            final TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
            tv.setTextColor(Color.WHITE);
        } catch (final NullPointerException ex) {
            LogUtil.i(getClass(), "Attempt to render offline snackbar into null view.");
        }
    }

    @Override
    protected void onStop() {
        this.connectionSubscription.unsubscribe();
        super.onStop();
    }
}