package com.tokenbrowser.model.local;

import io.realm.RealmList;
import io.realm.RealmObject;

public class CustomAppInformation extends RealmObject {
    private String paymentAddress;
    private String webApp;
    private String displayName;
    private String protocol;
    private String avatarUrl;
    private RealmList<RealmString> languages;
    private RealmList<RealmString> interfaces;
}
