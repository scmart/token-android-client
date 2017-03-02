package com.tokenbrowser.presenter;


import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.tokenbrowser.manager.SofaMessageManager;
import com.tokenbrowser.token.R;
import com.tokenbrowser.util.SoundManager;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.MainActivity;
import com.tokenbrowser.view.adapter.NavigationAdapter;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class MainPresenter implements Presenter<MainActivity> {
    private static final int DEFAULT_TAB = 0;

    private MainActivity activity;
    private boolean firstTimeAttached = true;
    private NavigationAdapter adapter;
    private CompositeSubscription subscriptions;

    private final AHBottomNavigation.OnTabSelectedListener tabListener = new AHBottomNavigation.OnTabSelectedListener() {
        @Override
        public boolean onTabSelected(final int position, final boolean wasSelected) {
            if (wasSelected) {
                return false;
            }

            final FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
            transaction.replace(activity.getBinding().container.getId(), adapter.getItem(position)).commit();
            SoundManager.getInstance().playSound(SoundManager.TAB_BUTTON);
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
        selectTabFromIntent();
        attachUnreadMessagesSubscription();
    }

    private void manuallySelectFirstTab() {
        this.tabListener.onTabSelected(DEFAULT_TAB, false);
    }

    private void selectTabFromIntent() {
        final Intent intent = this.activity.getIntent();
        final int activeTab = intent.getIntExtra(MainActivity.EXTRA__ACTIVE_TAB, this.activity.getBinding().navBar.getCurrentItem());
        this.activity.getIntent().removeExtra(MainActivity.EXTRA__ACTIVE_TAB);
        this.activity.getBinding().navBar.setCurrentItem(activeTab);
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
                messageManager
                    .isReady().toObservable()
                    .flatMap((unused) -> messageManager.registerForAllConversationChanges())
                    .flatMap((unused) -> messageManager.areUnreadMessages().toObservable())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleUnreadMessages);

        final Subscription firstTimeSubscription =
                messageManager
                .isReady().toObservable()
                .flatMap((unused) -> messageManager.areUnreadMessages().toObservable())
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
    public void onViewDestroyed() {
        this.subscriptions.clear();
        this.activity = null;
    }
}
