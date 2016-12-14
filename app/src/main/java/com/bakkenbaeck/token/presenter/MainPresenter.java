package com.bakkenbaeck.token.presenter;


import android.support.v4.content.ContextCompat;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.activity.MainActivity;

public class MainPresenter implements Presenter<MainActivity> {

    private MainActivity activity;
    private boolean firstTimeAttached = true;

    @Override
    public void onViewAttached(final MainActivity activity) {
        this.activity = activity;

        if (this.firstTimeAttached) {
            this.firstTimeAttached = false;
            initNavBar();
        }
    }

    private void initNavBar() {
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab_1, R.drawable.ic_action_home, 0);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab_2, R.drawable.ic_action_apps, 0);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab_3, R.drawable.ic_action_scan, 0);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.tab_4, R.drawable.ic_action_contacts, 0);
        AHBottomNavigationItem item5 = new AHBottomNavigationItem(R.string.tab_5, R.drawable.ic_action_settings, 0);

        final AHBottomNavigation navBar = this.activity.getBinding().navBar;
        navBar.addItem(item1);
        navBar.addItem(item2);
        navBar.addItem(item3);
        navBar.addItem(item4);
        navBar.addItem(item5);
        navBar.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        navBar.setAccentColor(ContextCompat.getColor(this.activity, R.color.colorPrimary));
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
