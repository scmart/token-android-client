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

import com.tokenbrowser.exception.InvalidQrCodePayment;
import com.tokenbrowser.util.EthUtil;

public class QrCodePayment {
    private String username;
    private String address;
    private String value;
    private String memo;

    public String getUsername() {
        return username;
    }

    public QrCodePayment setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public QrCodePayment setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getValue() throws InvalidQrCodePayment {
        if (isValueHex()) return this.value;
        try {
            return EthUtil.encodeToHex(this.value);
        } catch (NumberFormatException | NullPointerException e) {
            throw new InvalidQrCodePayment(e);
        }
    }

    public QrCodePayment setValue(String value) {
        this.value = value;
        return this;
    }

    private boolean isValueHex() {
        return this.value != null && this.value.startsWith("0x");
    }

    public String getMemo() {
        return memo;
    }

    public QrCodePayment setMemo(String memo) {
        this.memo = memo;
        return this;
    }

    public boolean isValid() {
        return this.value != null
                && (this.address != null || this.username != null);
    }
}
