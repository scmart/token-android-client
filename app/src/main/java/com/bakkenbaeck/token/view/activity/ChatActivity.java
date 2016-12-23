package com.bakkenbaeck.token.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.ActivityChatBinding;
import com.bakkenbaeck.token.model.Contact;
import com.bakkenbaeck.token.presenter.ChatPresenter;
import com.bakkenbaeck.token.presenter.factory.ChatPresenterFactory;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;

public final class ChatActivity extends BasePresenterActivity<ChatPresenter, ChatActivity> {
    public static final String EXTRA__CONTACT = "contact";

    private static final int UNIQUE_ACTIVITY_ID = 4002;
    private ActivityChatBinding binding;
    private Contact contact;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        this.contact = getIntent().getParcelableExtra(EXTRA__CONTACT);
    }

    public final ActivityChatBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<ChatPresenter> getPresenterFactory() {
        return new ChatPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final ChatPresenter presenter) {
        presenter.setPassedInContact(this.contact);
    }

    @Override
    protected int loaderId() {
        return UNIQUE_ACTIVITY_ID;
    }
}
