package com.bakkenbaeck.token.view.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.PathInterpolator;

import com.bakkenbaeck.token.BuildConfig;
import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.ActivityChatBinding;
import com.bakkenbaeck.token.model.ActivityResultHolder;
import com.bakkenbaeck.token.presenter.ChatPresenter;
import com.bakkenbaeck.token.presenter.PresenterLoader;
import com.bakkenbaeck.token.presenter.factory.ChatPresenterFactory;
import com.bakkenbaeck.token.view.Animation.SlideUpAnimator;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.Fragment.QrFragment;
import com.bakkenbaeck.token.view.adapter.viewholder.BottomOffsetDecoration;
import com.bakkenbaeck.token.view.custom.SpeedyLinearLayoutManager;
import com.bakkenbaeck.token.view.dialog.PhoneInputDialog;
import com.bakkenbaeck.token.view.dialog.VerificationCodeDialog;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public final class ChatActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ChatPresenter>, PhoneInputDialog.Listener,
        VerificationCodeDialog.Listener{
    private static final String TAG = "ChatActivity";
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

        SpeedyLinearLayoutManager linearLayoutManager = new SpeedyLinearLayoutManager(this);
        this.binding.messagesList.setLayoutManager(linearLayoutManager);

        SlideUpAnimator anim;

        if(Build.VERSION.SDK_INT >= 21){
            anim = new SlideUpAnimator(new PathInterpolator(0.33f, 0.78f, 0.3f, 1));
        }else{
            anim = new SlideUpAnimator(new DecelerateInterpolator());
        }

        anim.setAddDuration(400);
        this.binding.messagesList.setItemAnimator(anim);

        float offsetPx = getResources().getDimension(R.dimen.bottom_offset_dp);
        BottomOffsetDecoration bottomOffsetDecoration = new BottomOffsetDecoration((int) offsetPx);
        getBinding().messagesList.addItemDecoration(bottomOffsetDecoration);
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
        if (this.presenter.isAttached()) {
            this.presenter.handleActivityResult(new ActivityResultHolder(requestCode, resultCode, data));
        } else {
            // Will get processed when the activity attaches
            this.activityResultHolder = new ActivityResultHolder(requestCode, resultCode, data);
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

    @Override
    public void onPhoneInputSuccess(PhoneInputDialog dialog) {
        if (this.presenter == null) {
            return;
        }
        this.presenter.onPhoneInputSuccess(dialog);
    }

    @Override
    public void onVerificationCodeSuccess(VerificationCodeDialog dialog) {
        if (this.presenter == null) {
            return;
        }
        this.presenter.onVerificationSuccess();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        QrFragment qrFragment = (QrFragment) fm.findFragmentByTag(QrFragment.TAG);

        if(qrFragment != null){
            this.presenter.removeQrFragment();
        }else{
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy(){

        BaseApplication.get().disconnectWebSocket();

        super.onDestroy();
    }
}
