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

package com.tokenbrowser.util;


import rx.Subscriber;

public abstract class OnNextSubscriber<T> extends Subscriber<T> {

    @Override
    public void onCompleted() {
        final String programmerFriendlyMessage = "Unhandled onCompleted. This is potentially a programming error.";
        LogUtil.w(getClass(), programmerFriendlyMessage);
    }

    @Override
    public void onError(final Throwable e) {
        LogUtil.e(getClass(), "Unhandled onError " + e);
    }
}
