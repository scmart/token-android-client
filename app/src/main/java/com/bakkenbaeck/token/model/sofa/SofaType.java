package com.bakkenbaeck.token.model.sofa;


import android.support.annotation.IntDef;

public class SofaType {

    @IntDef({
            UNKNOWN,
            PLAIN_TEXT,
            PAYMENT_REQUEST,
            COMMAND_REQUEST,
            PAYMENT,
    })
    public @interface Type {}
    public static final int UNKNOWN = -1;
    public static final int PLAIN_TEXT = 0;
    public static final int PAYMENT_REQUEST = 1;
    public static final int COMMAND_REQUEST = 2;
    public static final int PAYMENT = 3;

    public static final String LOCAL_ONLY_PAYLOAD = "custom_local_only_payload";
    public static final String WEB_VIEW = "webview:";

    private static final String plain_text = "SOFA::Message:";
    private static final String command_request = "SOFA::Command:";
    private static final String payment_request = "SOFA::PaymentRequest:";
    private static final String payment = "SOFA::Payment:";

    public static String createHeader(final @SofaType.Type int type) {
        switch (type) {
            case PLAIN_TEXT: return plain_text;
            case PAYMENT_REQUEST: return payment_request;
            case COMMAND_REQUEST: return command_request;
            case PAYMENT: return payment;
            default: return null;
        }
    }
}
