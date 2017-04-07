package com.tokenbrowser.exception;

public class InvalidQrCode extends Exception {
    public InvalidQrCode(Throwable cause) {
        super(cause);
    }

    public InvalidQrCode() {
        super(new Throwable("Invalid QR code"));
    }
}
