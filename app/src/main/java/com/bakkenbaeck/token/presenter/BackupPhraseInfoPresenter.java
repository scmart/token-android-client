package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.view.View;
import android.widget.RadioButton;

import com.bakkenbaeck.token.view.activity.BackupPhraseActivity;
import com.bakkenbaeck.token.view.activity.BackupPhraseInfoActivity;

public class BackupPhraseInfoPresenter implements Presenter<BackupPhraseInfoActivity> {

    private BackupPhraseInfoActivity activity;
    private boolean understand = false;

    @Override
    public void onViewAttached(BackupPhraseInfoActivity view) {
        this.activity = view;
        initClickListeners();
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(this::handleCloseButtonClosed);
        this.activity.getBinding().radioButtonUnderstand.setOnClickListener(this::handleRadioButtonClicked);
        this.activity.getBinding().continueBtn.setOnClickListener(this::handleContinueClicked);
    }

    private void handleCloseButtonClosed(final View v) {
        this.activity.finish();
    }

    private void handleRadioButtonClicked(final View v) {
        this.understand = !this.understand;
        ((RadioButton)v).setChecked(this.understand);
    }

    private void handleContinueClicked(final View v) {
        if (!understand) {
            return;
        }
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
