package com.bakkenbaeck.token.view.adapter;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.bakkenbaeck.token.view.fragment.PlaceholderFragment;
import com.bakkenbaeck.token.view.fragment.ScannerFragment;

import java.util.ArrayList;

public class NavigationAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments = new ArrayList<>();

    public NavigationAdapter(final FragmentManager fm) {
        super(fm);

        fragments.clear();
        fragments.add(PlaceholderFragment.newInstance(0));
        fragments.add(PlaceholderFragment.newInstance(1));
        fragments.add(ScannerFragment.newInstance());
        fragments.add(PlaceholderFragment.newInstance(3));
        fragments.add(PlaceholderFragment.newInstance(4));
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