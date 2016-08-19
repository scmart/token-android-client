package com.bakkenbaeck.toshi.presenter.factory;

import com.bakkenbaeck.toshi.presenter.ChatPresenter;

public final class ChatPresenterFactory implements PresenterFactory<ChatPresenter> {

    public ChatPresenterFactory() {}

    @Override
    public ChatPresenter create() {
        return new ChatPresenter();
    }
}
