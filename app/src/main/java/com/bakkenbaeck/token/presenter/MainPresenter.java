package com.bakkenbaeck.token.presenter;


import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.util.SoundManager;
import com.bakkenbaeck.token.view.activity.MainActivity;
import com.bakkenbaeck.token.view.adapter.NavigationAdapter;

public class MainPresenter implements Presenter<MainActivity> {
    private final int DEFAULT_TAB = 0;

    private MainActivity activity;
    private boolean firstTimeAttached = true;
    private NavigationAdapter adapter;

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
        }
        initNavBar();
        selectCorrectTab();
    }

    private void selectCorrectTab() {
        final Intent intent = this.activity.getIntent();
        final int activeTab = intent.getIntExtra(MainActivity.EXTRA__ACTIVE_TAB, DEFAULT_TAB);
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

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        this.activity = null;
    }
}
