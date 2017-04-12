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
