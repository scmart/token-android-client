package com.tokenbrowser.presenter;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.tokenbrowser.model.local.Conversation;
import com.tokenbrowser.R;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.ChatActivity;
import com.tokenbrowser.view.activity.ScannerActivity;
import com.tokenbrowser.view.activity.UserSearchActivity;
import com.tokenbrowser.view.adapter.RecentAdapter;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;
import com.tokenbrowser.view.custom.HorizontalLineDivider;
import com.tokenbrowser.view.fragment.toplevel.RecentFragment;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public final class RecentPresenter implements
        Presenter<RecentFragment>,
        OnItemClickListener<Conversation> {

    private RecentFragment fragment;
    private boolean firstTimeAttaching = true;
    private RecentAdapter adapter;
    private CompositeSubscription subscriptions;

    @Override
    public void onViewAttached(final RecentFragment fragment) {
        this.fragment = fragment;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }
        initShortLivingObjects();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
        this.adapter = new RecentAdapter()
                .setOnItemClickListener(this);
    }

    private void initShortLivingObjects() {
        initRecentsAdapter();
        populateRecentsAdapter();
        attachSubscriber();
    }

    private void initRecentsAdapter() {
        final RecyclerView recyclerView = this.fragment.getBinding().recents;
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.fragment.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(this.adapter);

        final int dividerLeftPadding = this.fragment.getResources().getDimensionPixelSize(R.dimen.avatar_size_small)
                                     + this.fragment.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        final HorizontalLineDivider lineDivider =
                new HorizontalLineDivider(ContextCompat.getColor(this.fragment.getContext(), R.color.divider))
                .setLeftPadding(dividerLeftPadding);
        recyclerView.addItemDecoration(lineDivider);
    }

    private void populateRecentsAdapter() {
        final Subscription sub =
                BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager()
                .loadAllConversations()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((conversations) -> {
                    this.adapter.setConversations(conversations);
                    updateEmptyState();
                });
        this.subscriptions.add(sub);
    }

    private void attachSubscriber() {
        final Subscription sub =
                BaseApplication
                        .get()
                        .getTokenManager()
                        .getSofaMessageManager()
                        .registerForAllConversationChanges()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe((updatedConversation) -> {
                            this.adapter.updateConversation(updatedConversation);
                            updateEmptyState();
                        });
        this.subscriptions.add(sub);
    }

    private void updateEmptyState() {
        if (this.fragment == null) {
            return;
        }
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
    public void onItemClick(final Conversation clickedConversation) {
        final Intent intent = new Intent(this.fragment.getActivity(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS, clickedConversation.getMember().getTokenId());
        this.fragment.startActivity(intent);
    }

    @Override
    public void onViewDetached() {
        this.subscriptions.clear();
        this.fragment = null;
    }

    @Override
    public void onDestroyed() {
        this.subscriptions = null;
        this.adapter = null;
    }

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
        final Intent intent = new Intent(fragment.getActivity(), UserSearchActivity.class);
        fragment.startActivity(intent);
    }

    private void startScanQrActivity() {
        final Intent intent = new Intent(fragment.getActivity(), ScannerActivity.class);
        intent.putExtra(ScannerActivity.RESULT_TYPE, ScannerActivity.REDIRECT);
        fragment.getActivity().startActivity(intent);
    }
}
