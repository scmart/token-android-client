package com.tokenbrowser.presenter.factory;

import com.tokenbrowser.presenter.PaymentRequestConfirmPresenter;

public class PaymentRequestConfirmPresenterFactory implements PresenterFactory<PaymentRequestConfirmPresenter> {
    @Override
    public PaymentRequestConfirmPresenter create() {
        return new PaymentRequestConfirmPresenter();
    }
}
