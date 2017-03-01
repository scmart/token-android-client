package com.tokenbrowser.presenter;

import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.CompoundButton;

import com.tokenbrowser.token.R;
import com.tokenbrowser.view.activity.BackupPhraseActivity;
import com.tokenbrowser.view.activity.BackupPhraseInfoActivity;

public class BackupPhraseInfoPresenter implements Presenter<BackupPhraseInfoActivity> {

    private BackupPhraseInfoActivity activity;

    @Override
    public void onViewAttached(BackupPhraseInfoActivity view) {
        this.activity = view;
        initClickListeners();
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(this::handleCloseButtonClosed);
        this.activity.getBinding().radioButtonUnderstand.setOnCheckedChangeListener(this::handleCheckboxChecked);
        this.activity.getBinding().continueBtn.setOnClickListener(this::handleContinueClicked);
    }

    private void handleCloseButtonClosed(final View v) {
        this.activity.finish();
    }

    private void handleCheckboxChecked(final CompoundButton button, final boolean isChecked) {
        final @DrawableRes int imageResource = isChecked
                ? R.drawable.background_with_radius_primary_color
                : R.drawable.background_with_radius_disabled;
        this.activity.getBinding().continueBtn.setBackgroundResource(imageResource);
        this.activity.getBinding().continueBtn.setClickable(isChecked);
    }

    private void handleContinueClicked(final View v) {
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
