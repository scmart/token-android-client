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
                    final String text = "\uD83E\uDD11<br><br>Use the <a href=\"https://token-eth-faucet.herokuapp.com?wei=100000000000000000&address=" + wallet.getWalletAddress() + "\">Faucet</a> to add funds to your wallet.";
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
