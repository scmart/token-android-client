package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.view.activity.BackupPhraseVerifyActivity;

public class BackupPhraseVerifyPresenter implements Presenter<BackupPhraseVerifyActivity> {

    private BackupPhraseVerifyActivity activity;

    @Override
    public void onViewAttached(BackupPhraseVerifyActivity view) {
        this.activity = view;
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        this.activity = null;
    }
}
