package com.tokenbrowser.view.fragment.toplevel;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.token.R;
import com.tokenbrowser.token.databinding.FragmentTopLevelBinding;
import com.tokenbrowser.presenter.ContactsPresenter;
import com.tokenbrowser.presenter.factory.ContactsPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.view.fragment.BasePresenterFragment;

public class ContactsContainerFragment extends BasePresenterFragment<ContactsPresenter, ContactsContainerFragment> {

    private FragmentTopLevelBinding binding;

    public static ContactsContainerFragment newInstance() {
        return new ContactsContainerFragment();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container,
                             final Bundle inState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_top_level, container, false);
        this.binding.title.setText(R.string.contacts_title);
        return binding.getRoot();
    }

    public FragmentTopLevelBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<ContactsPresenter> getPresenterFactory() {
        return new ContactsPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final ContactsPresenter presenter) {}

    @Override
    protected int loaderId() {
        return 4;
    }
}
