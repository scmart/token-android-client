package com.tokenbrowser.view.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;

import com.tokenbrowser.view.fragment.children.SettingsFragment;
import com.tokenbrowser.view.fragment.toplevel.AppsFragment;
import com.tokenbrowser.view.fragment.toplevel.ContactsFragment;
import com.tokenbrowser.view.fragment.toplevel.HomeFragment;
import com.tokenbrowser.view.fragment.toplevel.RecentFragment;

import java.util.ArrayList;

public class NavigationAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments = new ArrayList<>();

    public NavigationAdapter(final AppCompatActivity activity, final int menuRes) {
        super(activity.getSupportFragmentManager());

        final PopupMenu popupMenu = new PopupMenu(activity, activity.findViewById(android.R.id.content));
        final Menu menu = popupMenu.getMenu();
        activity.getMenuInflater().inflate(menuRes, menu);

        fragments.clear();
        fragments.add(HomeFragment.newInstance());
        fragments.add(RecentFragment.newInstance());
        fragments.add(AppsFragment.newInstance());
        fragments.add(ContactsFragment.newInstance());
        fragments.add(SettingsFragment.newInstance());
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