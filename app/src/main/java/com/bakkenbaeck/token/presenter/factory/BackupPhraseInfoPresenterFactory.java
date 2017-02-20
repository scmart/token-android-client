package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.BackupPhraseInfoPresenter;

public class BackupPhraseInfoPresenterFactory implements PresenterFactory<BackupPhraseInfoPresenter> {
    @Override
    public BackupPhraseInfoPresenter create() {
        return new BackupPhraseInfoPresenter();
    }
}
