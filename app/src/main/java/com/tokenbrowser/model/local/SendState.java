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

package com.tokenbrowser.model.local;


import android.support.annotation.IntDef;

public class SendState {

    @IntDef({
            STATE_SENDING,
            STATE_SENT,
            STATE_FAILED,
            STATE_RECEIVED,
            STATE_LOCAL_ONLY,
            STATE_PENDING
    })
    public @interface State {}

    public static final int STATE_SENDING = 0;
    public static final int STATE_SENT = 1;
    public static final int STATE_FAILED = 2;
    public static final int STATE_RECEIVED = 3;
    public static final int STATE_LOCAL_ONLY = 4;
    public static final int STATE_PENDING = 5;
}
