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

package com.tokenbrowser.manager.model;


import android.support.annotation.IntDef;

import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.sofa.Payment;

public class PaymentTask {


    @IntDef({INCOMING, OUTGOING, OUTGOING_EXTERNAL})
    public @interface Action {}
    public static final int INCOMING = 0;
    public static final int OUTGOING = 1;
    public static final int OUTGOING_EXTERNAL = 2;

    private final User user;
    private final Payment payment;
    private final @Action int action;

    public PaymentTask(
            final User user,
            final Payment payment,
            final @Action int action) {
        this.user = user;
        this.payment = payment;
        this.action = action;
    }

    public PaymentTask(
            final Payment payment,
            final @Action int action) {
        this.user = null;
        this.payment = payment;
        this.action = action;
    }

    public User getUser() {
        return this.user;
    }

    public Payment getPayment() {
        return this.payment;
    }

    public int getAction() {
        return this.action;
    }
}
