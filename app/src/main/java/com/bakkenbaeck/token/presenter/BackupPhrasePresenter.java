package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.view.activity.BackupPhraseActivity;

public class BackupPhrasePresenter implements Presenter<BackupPhraseActivity> {

    private BackupPhraseActivity activity;

    @Override
    public void onViewAttached(BackupPhraseActivity view) {
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
