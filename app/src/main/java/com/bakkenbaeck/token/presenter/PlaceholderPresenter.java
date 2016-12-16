package com.bakkenbaeck.token.presenter;

import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.fragment.PlaceholderFragment;

public final class PlaceholderPresenter implements Presenter<PlaceholderFragment> {

    private PlaceholderFragment fragment;

    @Override
    public void onViewAttached(final PlaceholderFragment fragment) {
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
