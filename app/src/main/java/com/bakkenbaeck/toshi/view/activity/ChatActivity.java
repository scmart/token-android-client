package com.bakkenbaeck.toshi.view.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.bakkenbaeck.toshi.BuildConfig;
import com.bakkenbaeck.toshi.R;
import com.bakkenbaeck.toshi.databinding.ActivityChatBinding;
import com.bakkenbaeck.toshi.model.ActivityResultHolder;
import com.bakkenbaeck.toshi.presenter.ChatPresenter;
import com.bakkenbaeck.toshi.presenter.PresenterLoader;
import com.bakkenbaeck.toshi.presenter.factory.ChatPresenterFactory;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public final class ChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ChatPresenter> {

    private static final int UNIQUE_ACTIVITY_ID = 101;

    private ChatPresenter presenter;
    private ActivityChatBinding binding;

    private ActivityResultHolder activityResultHolder;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }
        init();
    }

    private void init() {
        getSupportLoaderManager().initLoader(UNIQUE_ACTIVITY_ID, null, this);
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);

        final RecyclerView.LayoutManager recyclerLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        this.binding.messagesList.setLayoutManager(recyclerLayoutManager);
    }

    public final ActivityChatBinding getBinding() {
        return this.binding;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.mi_withdraw) {
            this.presenter.handleWithdrawClicked();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public final void onStart() {
        super.onStart();
        this.presenter.onViewAttached(this);
        if (this.activityResultHolder != null) {
            this.presenter.handleActivityResult(activityResultHolder);
            this.activityResultHolder = null;
        }
    }

    @Override
    public final void onStop() {
        super.onStop();
        presenter.onViewDetached();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (this.presenter == null) {
            // Will get processed when the activity attaches
            this.activityResultHolder = new ActivityResultHolder(requestCode, resultCode, data);
        } else {
            this.presenter.handleActivityResult(new ActivityResultHolder(requestCode, resultCode, data));
        }
    }

    @Override
    public Loader<ChatPresenter> onCreateLoader(final int id, final Bundle args) {
        return new PresenterLoader<>(this, new ChatPresenterFactory());
    }

    @Override
    public void onLoadFinished(final Loader<ChatPresenter> loader, final ChatPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onLoaderReset(final Loader<ChatPresenter> loader) {
        this.presenter.onViewDestroyed();
        this.presenter = null;
    }
}
