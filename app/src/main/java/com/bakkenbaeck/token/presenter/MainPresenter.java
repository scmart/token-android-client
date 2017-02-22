package com.bakkenbaeck.token.presenter;


import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.local.SofaMessage;
import com.bakkenbaeck.token.model.network.UserSearchResults;
import com.bakkenbaeck.token.model.sofa.Message;
import com.bakkenbaeck.token.model.sofa.SofaAdapters;
import com.bakkenbaeck.token.network.IdService;
import com.bakkenbaeck.token.util.SharedPrefsUtil;
import com.bakkenbaeck.token.util.SoundManager;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.MainActivity;
import com.bakkenbaeck.token.view.adapter.NavigationAdapter;

import rx.schedulers.Schedulers;

public class MainPresenter implements Presenter<MainActivity> {
    private static final int DEFAULT_TAB = 0;
    private static final String ONBOARDING_BOT_NAME = "TestingBot";

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
            manuallySelectFirstTab();
            tryTriggerOnboarding();
        }
        initNavBar();
        selectCorrectTab();
    }

    private void manuallySelectFirstTab() {
        this.tabListener.onTabSelected(DEFAULT_TAB, false);
    }

    private void tryTriggerOnboarding() {
        if (SharedPrefsUtil.hasOnboarded()) {
            return;
        }

        IdService.getApi()
                .searchByUsername(ONBOARDING_BOT_NAME)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(this::handleOnboardingBotFound);
    }

    private void handleOnboardingBotFound(final UserSearchResults results) {
        results.getResults()
                .stream()
                .filter(user -> user.getUsernameForEditing().equals(ONBOARDING_BOT_NAME))
                .forEach(user -> {
                    BaseApplication
                            .get()
                            .getTokenManager()
                            .getSofaMessageManager()
                            .sendMessage(user, generateOnboardingMessage());
                    SharedPrefsUtil.setHasOnboarded();
                });
    }

    private SofaMessage generateOnboardingMessage() {
        final Message sofaMessage = new Message().setBody("");
        final String messageBody = new SofaAdapters().toJson(sofaMessage);
        return new SofaMessage().makeNew(true, messageBody);
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
