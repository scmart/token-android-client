package com.bakkenbaeck.token.presenter;

import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.fragment.NavigationFragment;

public final class NavigationPresenter implements Presenter<NavigationFragment> {

    private NavigationFragment fragment;

    @Override
    public void onViewAttached(final NavigationFragment fragment) {
        this.fragment = fragment;

        final TextView text = (TextView) this.fragment.getView().findViewById(R.id.qrCodeText);
        text.setText(text.getText().subSequence(0,1) + " - attached");
    }

    @Override
    public void onViewDetached() {
        this.fragment = null;
    }

    @Override
    public void onViewDestroyed() {
        this.fragment = null;
    }
}
