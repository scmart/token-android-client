package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.view.View;
import android.widget.CheckBox;

import com.bakkenbaeck.token.view.activity.BackupPhraseActivity;
import com.bakkenbaeck.token.view.activity.BackupPhraseInfoActivity;

public class BackupPhraseInfoPresenter implements Presenter<BackupPhraseInfoActivity> {

    private BackupPhraseInfoActivity activity;

    @Override
    public void onViewAttached(BackupPhraseInfoActivity view) {
        this.activity = view;
        initClickListeners();
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(this::handleCloseButtonClosed);
        this.activity.getBinding().continueBtn.setOnClickListener(this::handleContinueClicked);
    }

    private void handleCloseButtonClosed(final View v) {
        this.activity.finish();
    }

    private void handleContinueClicked(final View v) {
        final CheckBox checkBox = this.activity.getBinding().radioButtonUnderstand;
        if (!checkBox.isChecked()) {
            return;
        }

        final Intent intent = new Intent(this.activity, BackupPhraseActivity.class);
        this.activity.startActivity(intent);
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
