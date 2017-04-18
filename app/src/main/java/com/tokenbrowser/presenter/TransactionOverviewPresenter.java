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

package com.tokenbrowser.presenter;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tokenbrowser.R;
import com.tokenbrowser.model.local.PendingTransaction;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.TransactionOverviewActivity;
import com.tokenbrowser.view.adapter.TransactionsAdapter;
import com.tokenbrowser.view.custom.HorizontalLineDivider;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public class TransactionOverviewPresenter implements Presenter<TransactionOverviewActivity> {

    private TransactionOverviewActivity activity;
    private boolean firstTimeAttaching = true;
    private CompositeSubscription subscriptions;
    private TransactionsAdapter adapter;

    @Override
    public void onViewAttached(TransactionOverviewActivity view) {
        this.activity = view;
        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }
        initShortLivingObjects();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
        this.adapter = new TransactionsAdapter();
    }

    private void initShortLivingObjects() {
        initClickListener();
        initRecyclerView();
        loadAllTransactions();
    }

    private void initClickListener() {
        this.activity.getBinding().closeButton.setOnClickListener(this::handleCloseButtonClicked);
    }

    private void handleCloseButtonClicked(final View v) {
        this.activity.finish();
    }

    private void initRecyclerView() {
        final RecyclerView recyclerView = this.activity.getBinding().transactions;
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.activity);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(this.adapter);

        final int dividerLeftPadding = this.activity.getResources().getDimensionPixelSize(R.dimen.avatar_size_small)
                + this.activity.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        final HorizontalLineDivider lineDivider =
                new HorizontalLineDivider(ContextCompat.getColor(this.activity, R.color.divider))
                        .setLeftPadding(dividerLeftPadding);
        recyclerView.addItemDecoration(lineDivider);
    }

    private void loadAllTransactions() {
        final Subscription sub =
                BaseApplication
                .get()
                .getTokenManager()
                .getTransactionManager()
                .getAllTransactions()
                .doOnSubscribe(() -> this.adapter.clear())
                .doOnCompleted(this::updateEmptyState)
                .subscribe(this::handleTransactionLoaded);

        this.subscriptions.add(sub);
    }

    private void handleTransactionLoaded(final PendingTransaction transaction) {
        this.adapter.addTransaction(transaction);
    }

    private void updateEmptyState() {
        // Hide empty state if we have some content
        final boolean showingEmptyState = this.activity.getBinding().emptyStateSwitcher.getCurrentView().getId() == this.activity.getBinding().emptyState.getId();
        final boolean shouldShowEmptyState = this.adapter.getItemCount() == 0;

        if (shouldShowEmptyState && !showingEmptyState) {
            this.activity.getBinding().emptyStateSwitcher.showPrevious();
        } else if (!shouldShowEmptyState && showingEmptyState) {
            this.activity.getBinding().emptyStateSwitcher.showNext();
        }
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
        this.subscriptions.clear();
    }

    @Override
    public void onDestroyed() {
        this.activity = null;
        this.subscriptions = null;
    }
}
