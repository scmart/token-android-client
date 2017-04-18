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

import android.content.Context;
import android.support.v4.content.Loader;

import com.tokenbrowser.presenter.factory.PresenterFactory;

public class PresenterLoader<T extends Presenter> extends Loader<T> {

    private final PresenterFactory<T> factory;
    private T presenter;

    public PresenterLoader(final Context context, final PresenterFactory<T> factory) {
        super(context);
        this.factory = factory;
    }

    @Override
    protected void onStartLoading() {
        // Returns the presenter if one exists
        // otherwise it loads and returns a new presenter
        if (this.presenter != null) {
            deliverResult(this.presenter);
            return;
        }

        forceLoad();
    }

    @Override
    protected void onForceLoad() {
        this.presenter = this.factory.create();
        deliverResult(this.presenter);
    }

    @Override
    protected void onReset() {
        if (this.presenter != null) {
            this.presenter.onDestroyed();
            this.presenter = null;
        }
    }
}