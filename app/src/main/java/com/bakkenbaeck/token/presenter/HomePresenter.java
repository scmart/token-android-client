package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.view.fragment.toplevel.HomeFragment;

public class HomePresenter implements Presenter<HomeFragment> {
    private HomeFragment fragment;

    @Override
    public void onViewAttached(HomeFragment view) {
        this.fragment = view;
    }

    @Override
    public void onViewDetached() {
        this.fragment = null;
    }

    @Override
    public void onViewDestroyed() {
        this.fragment = null;
    }
}
