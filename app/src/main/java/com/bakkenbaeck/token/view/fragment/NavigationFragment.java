package com.bakkenbaeck.token.view.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bakkenbaeck.token.R;

public class NavigationFragment extends Fragment {

    public static NavigationFragment newInstance(final int position) {
        final NavigationFragment f = new NavigationFragment();
        final Bundle args = new Bundle();
        args.putInt("pos", position);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, final @Nullable Bundle inState) {
        final int position = getArguments().getInt("pos", 100);
        final View v =  inflater.inflate(R.layout.fragment_qr, container, false);
        ((TextView)v.findViewById(R.id.qrCodeText)).setText(String.valueOf(position));
        return v;
    }
}
