package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.BackupPhrasePresenter;

public class BackupPhrasePresenterFactory implements PresenterFactory<BackupPhrasePresenter> {
    @Override
    public BackupPhrasePresenter create() {
        return new BackupPhrasePresenter();
    }
}
