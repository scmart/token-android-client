package com.tokenbrowser.crypto.signal;


import android.preference.PreferenceManager;
import android.util.Base64;

import com.tokenbrowser.view.BaseApplication;

public class SignalPreferences {

    private static final String LOCAL_REGISTRATION_ID_PREF = "pref_local_registration_id";
    private static final String SERIALIZED_IDENTITY_KEY_PAIR_PREF = "serialized_identity_key_pair_pref";
    private static final String SERIALIZED_LAST_RESORT_KEY_PREF = "serialized_last_resort_key_pref";
    private static final String SIGNALING_KEY_PREF = "signaling_key_pref";
    private static final String SIGNED_PRE_KEY_ID_PREF = "signed_pre_key_id";
    private static final String PASSWORD_PREF = "password_pref";
    private static final String REGISTERED_WITH_SERVER_PREF = "have_registered_with_server_pref";


    public static boolean getRegisteredWithServer() {
        return getBooleanPreference(REGISTERED_WITH_SERVER_PREF, false);
    }

    public static void setRegisteredWithServer() {
        setBooleanPreference(REGISTERED_WITH_SERVER_PREF, true);
    }

    public static int getLocalRegistrationId() {
        return getIntegerPreference(LOCAL_REGISTRATION_ID_PREF, -1);
    }

    public static void setLocalRegistrationId(final int registrationId) {
        setIntegerPreference(LOCAL_REGISTRATION_ID_PREF, registrationId);
    }

    public static String getSignalingKey() {
        return getStringPreference(SIGNALING_KEY_PREF, null);
    }

    public static void setSignalingKey(final String signalingKey) {
        setStringPreference(SIGNALING_KEY_PREF, signalingKey);
    }

    public static String getPassword() {
        return getStringPreference(PASSWORD_PREF, null);
    }

    public static void setPassword(final String password) {
        setStringPreference(PASSWORD_PREF, password);
    }

    public static byte[] getSerializedIdentityKeyPair() {
        return getByteArrayPreference(SERIALIZED_IDENTITY_KEY_PAIR_PREF);
    }

    public static void setSerializedIdentityKeyPair(final byte[] serializedIdentityKeyPair) {
        setByteArrayPreference(SERIALIZED_IDENTITY_KEY_PAIR_PREF, serializedIdentityKeyPair);
    }

    public static byte[] getSerializedLastResortKey() {
        return getByteArrayPreference(SERIALIZED_LAST_RESORT_KEY_PREF);
    }

    public static void setSerializedLastResortKey(final byte[] serializedLastResortKey) {
        setByteArrayPreference(SERIALIZED_LAST_RESORT_KEY_PREF, serializedLastResortKey);
    }

    public static int getSignedPreKeyId() {
        return getIntegerPreference(SIGNED_PRE_KEY_ID_PREF, -1);
    }

    public static void setSignedPreKeyId(final int signedPreKeyId) {
        setIntegerPreference(SIGNED_PRE_KEY_ID_PREF, signedPreKeyId);
    }

    private static void setByteArrayPreference(final String key, final byte[] value) {
        final String encoded = Base64.encodeToString(value, Base64.NO_WRAP);
        setStringPreference(key, encoded);
    }

    private static byte[] getByteArrayPreference(final String key) {
        final String encoded = getStringPreference(key, null);
        if (encoded == null) {
            return null;
        }

        return Base64.decode(encoded, Base64.NO_WRAP);
    }

    private static void setStringPreference(final String key, final String value) {
        PreferenceManager.getDefaultSharedPreferences(BaseApplication.get()).edit().putString(key, value).apply();
    }

    private static String getStringPreference(final String key, final String defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(BaseApplication.get()).getString(key, defaultValue);
    }

    private static int getIntegerPreference(final String key, final int defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(BaseApplication.get()).getInt(key, defaultValue);
    }

    private static void setIntegerPreference(final String key, final int value) {
        PreferenceManager.getDefaultSharedPreferences(BaseApplication.get()).edit().putInt(key, value).apply();
    }

    private static boolean getBooleanPreference(final String key, final boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(BaseApplication.get()).getBoolean(key, defaultValue);
    }

    private static void setBooleanPreference(final String key, final boolean value) {
        PreferenceManager.getDefaultSharedPreferences(BaseApplication.get()).edit().putBoolean(key, value).apply();
    }


}
