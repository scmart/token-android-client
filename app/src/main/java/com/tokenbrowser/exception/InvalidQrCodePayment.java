package com.tokenbrowser.exception;

public class InvalidQrCodePayment extends Exception {
    public InvalidQrCodePayment(Throwable cause) {
        super(cause);
    }

    public InvalidQrCodePayment() {
        super(new Throwable("Invalid QR code payment"));
    }
}
