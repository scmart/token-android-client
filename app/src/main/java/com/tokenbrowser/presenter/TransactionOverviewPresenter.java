package com.tokenbrowser.presenter;

import com.tokenbrowser.view.activity.TransactionOverviewActivity;

public class TransactionOverviewPresenter implements Presenter<TransactionOverviewActivity> {

    private TransactionOverviewActivity activity;

    @Override
    public void onViewAttached(TransactionOverviewActivity view) {
        this.activity = view;
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onDestroyed() {}
}
