package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.BackupPhraseInfoPresenter;

public class BackupPhraseInfoPresenterFactory implements PresenterFactory<BackupPhraseInfoPresenter> {
    @Override
    public BackupPhraseInfoPresenter create() {
        return new BackupPhraseInfoPresenter();
    }
}
