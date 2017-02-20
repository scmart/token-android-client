package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.BackupPhraseVerifyPresenter;

public class BackupPhraseVerifyPresenterFactory implements PresenterFactory<BackupPhraseVerifyPresenter> {
    @Override
    public BackupPhraseVerifyPresenter create() {
        return new BackupPhraseVerifyPresenter();
    }
}
