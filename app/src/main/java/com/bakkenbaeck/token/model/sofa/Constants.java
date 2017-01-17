package com.bakkenbaeck.token.model.sofa;


public class Constants {

    public static final String NAMESPACE = "SOFA::";
    public static final String REQUEST_TYPE = "TxRequest:";

    public static String createHeader(final String requestType) {
        return NAMESPACE + requestType;
    }
}
