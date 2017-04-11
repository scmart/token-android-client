package com.tokenbrowser.presenter;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.tokenbrowser.model.local.User;
import com.tokenbrowser.R;
import com.tokenbrowser.util.OnSingleClickListener;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.ScannerActivity;
import com.tokenbrowser.view.activity.UserSearchActivity;
import com.tokenbrowser.view.activity.ViewUserActivity;
import com.tokenbrowser.view.adapter.ContactsAdapter;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;
import com.tokenbrowser.view.custom.HorizontalLineDivider;
import com.tokenbrowser.view.fragment.toplevel.ContactsFragment;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public final class ContactsPresenter implements
        Presenter<ContactsFragment>,
        OnItemClickListener<User> {

    private ContactsFragment fragment;
    private boolean firstTimeAttaching = true;
    private ContactsAdapter adapter;
    private CompositeSubscription subscriptions;

    @Override
    public void onViewAttached(final ContactsFragment fragment) {
        this.fragment = fragment;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }
        initShortLivingObjects();
    }

    private void initShortLivingObjects() {
        initRecyclerView();
        loadContacts();
        this.fragment.getBinding().userSearch.setOnClickListener(this.handleUserSearchClicked);
    }

    private void initRecyclerView() {
        final RecyclerView recyclerView = this.fragment.getBinding().contacts;
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.fragment.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(this.adapter);

        final int dividerLeftPadding = fragment.getResources().getDimensionPixelSize(R.dimen.avatar_size_small)
                + fragment.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        final HorizontalLineDivider lineDivider =
                new HorizontalLineDivider(ContextCompat.getColor(this.fragment.getContext(), R.color.divider))
                        .setLeftPadding(dividerLeftPadding);
        recyclerView.addItemDecoration(lineDivider);
    }

    private void loadContacts() {
        final Subscription sub = BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .loadAllContacts()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(contacts -> {
                    this.adapter.mapContactsToUsers(contacts);
                    updateEmptyState();
                });

        this.subscriptions.add(sub);
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
        this.adapter = new ContactsAdapter()
                .setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(final User clickedUser) {
        final Intent intent = new Intent(this.fragment.getActivity(), ViewUserActivity.class);
        intent.putExtra(ViewUserActivity.EXTRA__USER_ADDRESS, clickedUser.getTokenId());
        this.fragment.startActivity(intent);
    }

    private void updateEmptyState() {
        if (this.fragment == null) return;
        
        // Hide empty state if we have some content
        final boolean showingEmptyState = this.fragment.getBinding().emptyStateSwitcher.getCurrentView().getId() == this.fragment.getBinding().emptyState.getId();
        final boolean shouldShowEmptyState = this.adapter.getItemCount() == 0;

        if (shouldShowEmptyState && !showingEmptyState) {
            this.fragment.getBinding().emptyStateSwitcher.showPrevious();
        } else if (!shouldShowEmptyState && showingEmptyState) {
            this.fragment.getBinding().emptyStateSwitcher.showNext();
        }
    }

    @Override
    public void onViewDetached() {
        this.subscriptions.clear();
        this.fragment = null;
    }

    @Override
    public void onDestroyed() {
        this.adapter = null;
        this.fragment = null;
    }

    private final OnSingleClickListener handleUserSearchClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            startUserSearchActivity();
        }
    };

    public void handleActionMenuClicked(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan_qr:
                startScanQrActivity();
                break;
            case R.id.invite_friends:
                handleInviteFriends();
                break;
            case R.id.search_people:
                startUserSearchActivity();
                break;
        }
    }

    private void handleInviteFriends() {
        final Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, this.fragment.getActivity().getString(R.string.invite_friends_intent_message));
        sendIntent.setType("text/plain");
        this.fragment.getActivity().startActivity(sendIntent);
    }

    private void startUserSearchActivity() {
        if (this.fragment == null) return;
        final Intent intent = new Intent(this.fragment.getActivity(), UserSearchActivity.class);
        fragment.startActivity(intent);
    }

    private void startScanQrActivity() {
        if (this.fragment == null) return;
        final Intent intent = new Intent(this.fragment.getActivity(), ScannerActivity.class);
        intent.putExtra(ScannerActivity.RESULT_TYPE, ScannerActivity.REDIRECT);
        this.fragment.getActivity().startActivity(intent);
    }
}
