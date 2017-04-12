package com.tokenbrowser.presenter;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.tokenbrowser.R;
import com.tokenbrowser.manager.SofaMessageManager;
import com.tokenbrowser.util.SharedPrefsUtil;
import com.tokenbrowser.util.SoundManager;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.MainActivity;
import com.tokenbrowser.view.activity.ScannerActivity;
import com.tokenbrowser.view.adapter.NavigationAdapter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class MainPresenter implements Presenter<MainActivity> {
    private static final int DEFAULT_TAB = 0;
    private static final int SCAN_POSITION = 2;

    private MainActivity activity;
    private boolean firstTimeAttached = true;
    private NavigationAdapter adapter;
    private CompositeSubscription subscriptions;

    private final AHBottomNavigation.OnTabSelectedListener tabListener = new AHBottomNavigation.OnTabSelectedListener() {
        @Override
        public boolean onTabSelected(final int position, final boolean wasSelected) {
            if (position == SCAN_POSITION) {
                openScanActivity();
                return false;
            }
            final FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(activity.getBinding().fragmentContainer.getId(), adapter.getItem(position)).commit();

            if (!wasSelected) {
                SoundManager.getInstance().playSound(SoundManager.TAB_BUTTON);
            }
            return true;
        }
    };

    @Override
    public void onViewAttached(final MainActivity activity) {
        this.activity = activity;

        if (this.firstTimeAttached) {
            this.firstTimeAttached = false;
            this.adapter = new NavigationAdapter(this.activity, R.menu.navigation);
            this.subscriptions = new CompositeSubscription();
            manuallySelectFirstTab();
        }
        initNavBar();
        trySelectTabFromIntent();
        attachUnreadMessagesSubscription();
        showBetaWarningDialog();
    }

    private void manuallySelectFirstTab() {
        this.tabListener.onTabSelected(DEFAULT_TAB, false);
    }

    private void initNavBar() {
        final AHBottomNavigation navBar = this.activity.getBinding().navBar;
        final AHBottomNavigationAdapter menuInflater = new AHBottomNavigationAdapter(this.activity, R.menu.navigation);
        menuInflater.setupWithBottomNavigation(navBar);
        navBar.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        navBar.setAccentColor(ContextCompat.getColor(this.activity, R.color.colorPrimary));
        navBar.setOnTabSelectedListener(this.tabListener);
        navBar.setSoundEffectsEnabled(false);
        navBar.setBehaviorTranslationEnabled(false);
    }

    private void attachUnreadMessagesSubscription() {
        final SofaMessageManager messageManager =
                BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager();

        final Subscription allChangesSubscription =
                messageManager.registerForAllConversationChanges()
                .flatMap((unused) -> messageManager.areUnreadMessages().toObservable())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleUnreadMessages);

        final Subscription firstTimeSubscription =
                messageManager.areUnreadMessages().toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleUnreadMessages);

        this.subscriptions.add(allChangesSubscription);
        this.subscriptions.add(firstTimeSubscription);
    }

    private void handleUnreadMessages(final boolean areUnreadMessages) {
        if (areUnreadMessages) {
            showUnreadBadge();
        } else {
            hideUnreadBadge();
        }
    }

    private void showBetaWarningDialog() {
        if (SharedPrefsUtil.hasLoadedApp()) return;

        final AlertDialog.Builder builder = new AlertDialog.Builder(this.activity, R.style.AlertDialogCustom);
        builder.setTitle(R.string.beta_warning_title)
                .setMessage(R.string.beta_warning_message)
                .setPositiveButton(R.string.continue_, (dialog, which) -> {
                    dialog.dismiss();
                });
        builder.create().show();
        SharedPrefsUtil.setHasLoadedApp();
    }

    private void showUnreadBadge() {
        this.activity.getBinding().navBar.setNotification(" ", 1);
    }

    private void hideUnreadBadge() {
        this.activity.getBinding().navBar.setNotification("", 1);
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
        this.subscriptions.clear();
    }

    @Override
    public void onDestroyed() {
        this.adapter = null;
    }

    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        trySelectTabFromIntent();
    }

    private void trySelectTabFromIntent() {
        final Intent intent = this.activity.getIntent();
        final int activeTab = intent.getIntExtra(MainActivity.EXTRA__ACTIVE_TAB, this.activity.getBinding().navBar.getCurrentItem());
        this.activity.getIntent().removeExtra(MainActivity.EXTRA__ACTIVE_TAB);
        this.activity.getBinding().navBar.setCurrentItem(activeTab);
    }

    private void openScanActivity() {
        final Intent intent = new Intent(this.activity, ScannerActivity.class);
        this.activity.startActivity(intent);
    }
}
