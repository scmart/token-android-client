/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.view.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuItem;

import com.tokenbrowser.model.local.ActivityResultHolder;
import com.tokenbrowser.presenter.chat.ChatPresenter;
import com.tokenbrowser.presenter.factory.ChatPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.R;
import com.tokenbrowser.databinding.ActivityChatBinding;

public final class ChatActivity extends BasePresenterActivity<ChatPresenter, ChatActivity> {

    public static final String EXTRA__REMOTE_USER_ADDRESS = "remote_user_owner_address";
    public static final String EXTRA__PAYMENT_ACTION = "payment_action";
    public static final String EXTRA__ETH_AMOUNT = "eth_amount";
    public static final String EXTRA__PLAY_SCAN_SOUNDS = "play_scan_sounds";

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

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        this.presenter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        this.presenter.onRestoreInstanceState(savedInstanceState);
    }


}
