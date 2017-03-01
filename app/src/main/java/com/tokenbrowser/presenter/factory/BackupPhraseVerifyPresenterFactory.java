package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.BackupPhraseVerifyPresenter;

public class BackupPhraseVerifyPresenterFactory implements PresenterFactory<BackupPhraseVerifyPresenter> {
    @Override
    public BackupPhraseVerifyPresenter create() {
        return new BackupPhraseVerifyPresenter();
    }
}
