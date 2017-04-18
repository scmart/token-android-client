/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.view.fragment.toplevel;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.FragmentAppsBinding;
import com.tokenbrowser.presenter.AppsPresenter;
import com.tokenbrowser.presenter.factory.AppsPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.view.fragment.BasePresenterFragment;

public class AppsFragment extends BasePresenterFragment<AppsPresenter, AppsFragment> {

    private FragmentAppsBinding binding;

    public static AppsFragment newInstance() {
        return new AppsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, final @Nullable Bundle inState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_apps, container, false);
        return binding.getRoot();
    }

    public FragmentAppsBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<AppsPresenter> getPresenterFactory() {
        return new AppsPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull AppsPresenter presenter) {

    }

    @Override
    protected int loaderId() {
        return 2;
    }
}
