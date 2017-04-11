package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.TransactionOverviewPresenter;

public class TransactionOverviewPresenterFactory implements PresenterFactory<TransactionOverviewPresenter> {
    @Override
    public TransactionOverviewPresenter create() {
        return new TransactionOverviewPresenter();
    }
}
