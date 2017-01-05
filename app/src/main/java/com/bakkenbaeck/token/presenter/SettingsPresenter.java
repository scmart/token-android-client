package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.view.fragment.children.SettingsFragment;

public final class SettingsPresenter implements
        Presenter<SettingsFragment> {

    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(final SettingsFragment fragment) {

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
        }
    }

    @Override
    public void onViewDetached() {}

    @Override
    public void onViewDestroyed() {}
}
