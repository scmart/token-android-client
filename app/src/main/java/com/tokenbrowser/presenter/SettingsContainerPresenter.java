package com.tokenbrowser.presenter;

import android.support.v4.app.FragmentTransaction;

import com.tokenbrowser.view.fragment.children.SettingsFragment;
import com.tokenbrowser.view.fragment.toplevel.SettingsContainerFragment;

public final class SettingsContainerPresenter implements
        Presenter<SettingsContainerFragment> {

    private SettingsFragment settingsFragment;

    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(final SettingsContainerFragment fragment) {

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            manuallyAddRootFragment(fragment);
        }
    }

    private void manuallyAddRootFragment(final SettingsContainerFragment fragment) {
        this.settingsFragment = SettingsFragment.newInstance();

        final FragmentTransaction transaction = fragment.getChildFragmentManager().beginTransaction();
        transaction.replace(fragment.getBinding().container.getId(), settingsFragment).commit();
    }

    @Override
    public void onViewDetached() {}

    @Override
    public void onViewDestroyed() {}
}
