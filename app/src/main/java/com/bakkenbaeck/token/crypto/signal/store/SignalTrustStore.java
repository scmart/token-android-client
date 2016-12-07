package com.bakkenbaeck.token.crypto.signal.store;


import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.BaseApplication;

import org.whispersystems.signalservice.api.push.TrustStore;

import java.io.InputStream;

public class SignalTrustStore implements TrustStore {
    @Override
    public InputStream getKeyStoreInputStream() {
        return BaseApplication.get().getResources().openRawResource(R.raw.heroku);
    }

    @Override
    public String getKeyStorePassword() {
        return "whisper";
    }
}
