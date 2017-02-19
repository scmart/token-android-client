package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.BackupPhrasePresenter;

public class BackupPhrasePresenterFactory implements PresenterFactory<BackupPhrasePresenter> {
    @Override
    public BackupPhrasePresenter create() {
        return new BackupPhrasePresenter();
    }
}
