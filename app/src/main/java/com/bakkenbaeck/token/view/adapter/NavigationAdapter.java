package com.bakkenbaeck.token.view.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;

import com.bakkenbaeck.token.view.fragment.toplevel.ContactsContainerFragment;
import com.bakkenbaeck.token.view.fragment.toplevel.HomeFragment;
import com.bakkenbaeck.token.view.fragment.toplevel.PlaceholderFragment;
import com.bakkenbaeck.token.view.fragment.toplevel.ScannerFragment;
import com.bakkenbaeck.token.view.fragment.toplevel.SettingsContainerFragment;

import java.util.ArrayList;

public class NavigationAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments = new ArrayList<>();

    public NavigationAdapter(final AppCompatActivity activity, final int menuRes) {
        super(activity.getSupportFragmentManager());

        final PopupMenu popupMenu = new PopupMenu(activity, null);
        final Menu menu = popupMenu.getMenu();
        activity.getMenuInflater().inflate(menuRes, menu);

        fragments.clear();
        fragments.add(HomeFragment.newInstance());
        fragments.add(PlaceholderFragment.newInstance(menu.getItem(1).getTitle()));
        fragments.add(ScannerFragment.newInstance());
        fragments.add(ContactsContainerFragment.newInstance());
        fragments.add(SettingsContainerFragment.newInstance());
    }

    @Override
    public Fragment getItem(final int position) {
        return this.fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }
}