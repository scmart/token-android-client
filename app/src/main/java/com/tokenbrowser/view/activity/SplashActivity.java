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

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.BuildConfig;
import com.crashlytics.android.Crashlytics;
import com.tokenbrowser.presenter.SplashPresenter;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.presenter.factory.SplashPresenterFactory;

import io.fabric.sdk.android.Fabric;

public class SplashActivity extends BasePresenterActivity<SplashPresenter, SplashActivity> {

    public static final String EXTRA__NEXT_INTENT = "next_intent";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
    }

    @NonNull
    @Override
    protected PresenterFactory<SplashPresenter> getPresenterFactory() {
        return new SplashPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull SplashPresenter presenter) {}

    @Override
    protected int loaderId() {
        return 1;
    }
}
