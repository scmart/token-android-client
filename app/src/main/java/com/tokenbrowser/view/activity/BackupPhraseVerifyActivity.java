package com.tokenbrowser.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.token.R;
import com.tokenbrowser.token.databinding.ActivityBackupPhraseVerifyBinding;
import com.tokenbrowser.presenter.BackupPhraseVerifyPresenter;
import com.tokenbrowser.presenter.factory.BackupPhraseVerifyPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;

public class BackupPhraseVerifyActivity
        extends BasePresenterActivity<BackupPhraseVerifyPresenter, BackupPhraseVerifyActivity> {

    private ActivityBackupPhraseVerifyBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_backup_phrase_verify);
    }

    public ActivityBackupPhraseVerifyBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<BackupPhraseVerifyPresenter> getPresenterFactory() {
        return new BackupPhraseVerifyPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final BackupPhraseVerifyPresenter presenter) {}

    @Override
    protected int loaderId() {
        return 5007;
    }
}
