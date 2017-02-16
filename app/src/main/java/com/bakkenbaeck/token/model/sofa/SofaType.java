package com.bakkenbaeck.token.model.sofa;


import android.support.annotation.IntDef;

public class SofaType {

    @IntDef({
            UNKNOWN,
            PLAIN_TEXT,
            PAYMENT_REQUEST,
            COMMAND_REQUEST,
            PAYMENT,
            INIT,
            INIT_REQUEST,
    })
    public @interface Type {}
    public static final int UNKNOWN = -1;
    public static final int PLAIN_TEXT = 0;
    public static final int PAYMENT_REQUEST = 1;
    public static final int COMMAND_REQUEST = 2;
    public static final int PAYMENT = 3;
    public static final int INIT = 4;
    public static final int INIT_REQUEST = 5;

    public static final String LOCAL_ONLY_PAYLOAD = "custom_local_only_payload";
    public static final String WEB_VIEW = "webview:";
    public static final String PAYMENT_ADDRESS = "paymentAddress";
    public static final String LANGUAGE = "language";
    public static final String CONFIRMED = "confirmed";

    private static final String plain_text = "SOFA::Message:";
    private static final String command_request = "SOFA::Command:";
    private static final String payment_request = "SOFA::PaymentRequest:";
    private static final String payment = "SOFA::Payment:";
    private static final String init = "SOFA::Init:";
    private static final String init_request = "SOFA::InitRequest:";

    public static String createHeader(final @SofaType.Type int type) {
        switch (type) {
            case PLAIN_TEXT: return plain_text;
            case PAYMENT_REQUEST: return payment_request;
            case COMMAND_REQUEST: return command_request;
            case PAYMENT: return payment;
            case INIT: return init;
            case INIT_REQUEST: return init_request;
            default: return null;
        }
    }

    public static @Type int getType(final String header) {
        switch (header) {
            case plain_text: return PLAIN_TEXT;
            case payment_request: return PAYMENT_REQUEST;
            case command_request: return COMMAND_REQUEST;
            case payment: return PAYMENT;
            case init: return INIT;
            case init_request: return INIT_REQUEST;
            default: return UNKNOWN;
        }
    }
}
