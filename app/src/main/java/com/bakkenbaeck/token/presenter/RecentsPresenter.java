package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.view.fragment.toplevel.RecentsFragment;

public final class RecentsPresenter implements Presenter<RecentsFragment> {

    private RecentsFragment contactsListFragment;

    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(final RecentsFragment fragment) {

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
        }
    }

    @Override
    public void onViewDetached() {}

    @Override
    public void onViewDestroyed() {}
}
