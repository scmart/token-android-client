package com.bakkenbaeck.token.view.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;

import com.bakkenbaeck.token.view.fragment.ContactsFragment;
import com.bakkenbaeck.token.view.fragment.PlaceholderFragment;
import com.bakkenbaeck.token.view.fragment.ScannerFragment;

import java.util.ArrayList;

public class NavigationAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments = new ArrayList<>();

    public NavigationAdapter(final AppCompatActivity activity, final int menuRes) {
        super(activity.getSupportFragmentManager());

        final PopupMenu popupMenu = new PopupMenu(activity, null);
        final Menu menu = popupMenu.getMenu();
        activity.getMenuInflater().inflate(menuRes, menu);

        fragments.clear();
        fragments.add(PlaceholderFragment.newInstance(menu.getItem(0).getTitle()));
        fragments.add(PlaceholderFragment.newInstance(menu.getItem(1).getTitle()));
        fragments.add(ScannerFragment.newInstance(menu.getItem(2).getTitle()));
        fragments.add(ContactsFragment.newInstance(menu.getItem(3).getTitle()));
        fragments.add(PlaceholderFragment.newInstance(menu.getItem(4).getTitle()));
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