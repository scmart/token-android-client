package com.bakkenbaeck.token.view.adapter;


import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.bakkenbaeck.token.view.fragment.NavigationFragment;

import java.util.ArrayList;

public class NavigationAdapter extends FragmentPagerAdapter {

    private ArrayList<NavigationFragment> fragments = new ArrayList<>();

    public NavigationAdapter(final FragmentManager fm) {
        super(fm);

        fragments.clear();
        fragments.add(NavigationFragment.newInstance(0));
        fragments.add(NavigationFragment.newInstance(1));
        fragments.add(NavigationFragment.newInstance(2));
        fragments.add(NavigationFragment.newInstance(3));
        fragments.add(NavigationFragment.newInstance(4));
    }

    @Override
    public NavigationFragment getItem(final int position) {
        return this.fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }
}