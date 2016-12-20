package com.bakkenbaeck.token.presenter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.activity.MainActivity;
import com.bakkenbaeck.token.view.adapter.NavigationAdapter;

public class MainPresenter implements Presenter<MainActivity> {

    private MainActivity activity;
    private boolean firstTimeAttached = true;
    private NavigationAdapter adapter;
    private final int DEFAULT_TAB = 0;
    private int currentSelectedTab = DEFAULT_TAB;

    private final AHBottomNavigation.OnTabSelectedListener tabListener = new AHBottomNavigation.OnTabSelectedListener() {
        @Override
        public boolean onTabSelected(final int position, final boolean wasSelected) {
            if (wasSelected) {
                return false;
            }

            detachOldFragment(position);
            attachNewFragment(position);
            currentSelectedTab = position;
            return true;
        }

        private void attachNewFragment(final int position) {
            final FragmentManager fm = activity.getSupportFragmentManager();
            final Fragment newFragment = adapter.getItem(position);
            if (fm.findFragmentByTag(newFragment.getClass().getSimpleName()) == null) {
                fm.beginTransaction()
                    .add(activity.getBinding().container.getId(), newFragment, newFragment.getClass().getSimpleName())
                    .commit();
            } else {
                fm.beginTransaction()
                    .attach(fm.findFragmentByTag(newFragment.getClass().getSimpleName()))
                    .commit();
            }
        }

        private void detachOldFragment(final int position) {
            if (currentSelectedTab == position) {
                return;
            }

            final FragmentManager fm = activity.getSupportFragmentManager();
            final Fragment oldFragment = adapter.getItem(currentSelectedTab);
            fm.beginTransaction()
                .detach(oldFragment)
                .commit();
        }
    };

    @Override
    public void onViewAttached(final MainActivity activity) {
        this.activity = activity;

        if (this.firstTimeAttached) {
            this.firstTimeAttached = false;
            this.adapter = new NavigationAdapter(this.activity, R.menu.navigation);
            manuallySelectFirstTab();
        }
        initNavBar();
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
