package com.tokenbrowser.presenter;

import android.content.Intent;
import android.view.View;

import com.tokenbrowser.R;
import com.tokenbrowser.view.activity.TrustedFriendsActivity;

public class TrustedFriendsPresenter implements Presenter<TrustedFriendsActivity> {

    private TrustedFriendsActivity activity;

    @Override
    public void onViewAttached(TrustedFriendsActivity view) {
        this.activity = view;
        initClickListeners();
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(this::handleCloseButtonClicked);
        this.activity.getBinding().inviteFriendsBtn.setOnClickListener(this::handleInviteFriendsClicked);
    }

    private void handleCloseButtonClicked(final View v) {
        this.activity.finish();
    }

    private void handleInviteFriendsClicked(final View v) {
        final Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, this.activity.getString(R.string.invite_friends_intent_message));
        sendIntent.setType("text/plain");
        this.activity.startActivity(sendIntent);
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onDestroyed() {}
}
