package com.bakkenbaeck.token.presenter.factory;

import com.bakkenbaeck.token.presenter.ChatPresenter;

public final class ChatPresenterFactory implements PresenterFactory<ChatPresenter> {

    public ChatPresenterFactory() {}

    @Override
    public ChatPresenter create() {
        return new ChatPresenter();
    }
}
