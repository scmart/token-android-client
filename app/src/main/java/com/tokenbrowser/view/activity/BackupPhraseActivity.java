package com.tokenbrowser.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.tokenbrowser.token.R;
import com.tokenbrowser.token.databinding.ActivityBackupPhraseBinding;
import com.tokenbrowser.presenter.BackupPhrasePresenter;
import com.tokenbrowser.presenter.factory.BackupPhrasePresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;

public class BackupPhraseActivity extends BasePresenterActivity<BackupPhrasePresenter, BackupPhraseActivity> {

    private ActivityBackupPhraseBinding binding;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_backup_phrase);
    }

    public ActivityBackupPhraseBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<BackupPhrasePresenter> getPresenterFactory() {
        return new BackupPhrasePresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final BackupPhrasePresenter presenter) {}

    @Override
    protected int loaderId() {
        return 5006;
    }
}
