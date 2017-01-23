package com.bakkenbaeck.token.model.sofa;


import android.support.annotation.IntDef;

public class SofaType {

    @IntDef({
            UNKNOWN,
            PLAIN_TEXT,
            PAYMENT_REQUEST,
    })
    public @interface Type {}
    public static final int UNKNOWN = -1;
    public static final int PLAIN_TEXT = 0;
    public static final int PAYMENT_REQUEST = 1;

    public static final String LOCAL_ONLY_PAYLOAD = "custom_local_only_payload";

    private static final String plain_text = "SOFA::Message:";
    private static final String payment_request = "SOFA::PaymentRequest:";

    public static @SofaType.Type int getType(final String type) {
        switch (type) {
            case plain_text: return PLAIN_TEXT;
            case payment_request: return PAYMENT_REQUEST;
            default: return UNKNOWN;
        }
    }

    public static String createHeader(final @SofaType.Type int type) {
        switch (type) {
            case PLAIN_TEXT: return plain_text;
            case PAYMENT_REQUEST: return payment_request;
            default: return null;
        }
    }
}
