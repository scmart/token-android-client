package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.view.activity.BackupPhraseInfoActivity;

public class BackupPhraseInfoPresenter implements Presenter<BackupPhraseInfoActivity> {

    private BackupPhraseInfoActivity activity;

    @Override
    public void onViewAttached(BackupPhraseInfoActivity view) {
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
