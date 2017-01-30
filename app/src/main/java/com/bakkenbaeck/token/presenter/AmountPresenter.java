package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.view.activity.AmountActivity;

import java.util.Locale;

public class AmountPresenter implements Presenter<AmountActivity> {

    private AmountActivity activity;

    @Override
    public void onViewAttached(AmountActivity view) {
        this.activity = view;
        initView();
    }

    private void initView() {
        this.activity.getBinding().usdValue.setText(String.format(Locale.getDefault(), "%s %d", "$", 0));
        this.activity.getBinding().ethValue.setText(String.format(Locale.getDefault(), "%d %s", 0, "ETH"));
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
