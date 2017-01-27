package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.fragment.toplevel.PlaceholderFragment;

public final class PlaceholderPresenter implements Presenter<PlaceholderFragment> {

    private PlaceholderFragment fragment;

    @Override
    public void onViewAttached(final PlaceholderFragment fragment) {
        this.fragment = fragment;

        BaseApplication.get().getTokenManager().getWallet().subscribe(new SingleSuccessSubscriber<HDWallet>() {
            @Override
            public void onSuccess(final HDWallet wallet) {
                if (fragment != null) {
                    final String text = "To add funds to your wallet.\n\nGo to: https://token-eth-faucet.herokuapp.com?w=" + wallet.getWalletAddress();
                    fragment.setText(text);
                }
            }
        });
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
