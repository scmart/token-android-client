package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.fragment.toplevel.AppsFragment;

public class AppsPresenter implements Presenter<AppsFragment>{

    private AppsFragment fragment;

    @Override
    public void onViewAttached(AppsFragment view) {
        this.fragment = view;
        initToolbar();
    }

    private void initToolbar() {
        this.fragment.getBinding().title.setText(this.fragment.getString(R.string.tab_2));
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
