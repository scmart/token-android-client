package com.tokenbrowser.view.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import com.tokenbrowser.token.R;
import com.tokenbrowser.token.databinding.ActivityChatBinding;
import com.tokenbrowser.model.local.ActivityResultHolder;
import com.tokenbrowser.presenter.ChatPresenter;
import com.tokenbrowser.presenter.factory.ChatPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;

public final class ChatActivity extends BasePresenterActivity<ChatPresenter, ChatActivity> {

    public static final String EXTRA__REMOTE_USER_ADDRESS = "remote_user_owner_address";
    public static final String EXTRA__PAYMENT_ACTION = "payment_action";
    public static final String EXTRA__ETH_AMOUNT = "eth_amount";

    private ActivityChatBinding binding;
    private ActivityResultHolder resultHolder;
    private ChatPresenter presenter;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public void onResume() {
        super.onResume();
        tryProcessResultHolder();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
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
        this.presenter = presenter;
        tryProcessResultHolder();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        this.resultHolder = new ActivityResultHolder(requestCode, resultCode, data);
        tryProcessResultHolder();
    }

    private void tryProcessResultHolder() {
        if (this.presenter == null || this.resultHolder == null) {
            return;
        }

        if (this.presenter.handleActivityResult(this.resultHolder)) {
            this.resultHolder = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String permissions[],
                                           @NonNull final int[] grantResults) {
        if (this.presenter == null) {
            return;
        }

        this.presenter.handlePermission(requestCode, permissions, grantResults);
    }

    @Override
    protected int loaderId() {
        return 4002;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        this.presenter.handleActionMenuClicked(item);
        return super.onOptionsItemSelected(item);
    }
}
