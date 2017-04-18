/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.presenter;

import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.view.View;
import android.widget.CompoundButton;

import com.tokenbrowser.R;
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
        this.activity.getBinding().understandContainer.setOnClickListener(this::handleContainerChecked);
        this.activity.getBinding().radioButtonUnderstand.setOnCheckedChangeListener(this::handleCheckboxChecked);
        this.activity.getBinding().continueBtn.setOnClickListener(this::handleContinueClicked);
    }

    private void handleContainerChecked(final View view) {
        this.activity.getBinding().radioButtonUnderstand.performClick();
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
    public void onDestroyed() {}
}
