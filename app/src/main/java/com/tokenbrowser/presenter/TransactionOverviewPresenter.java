package com.tokenbrowser.presenter;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

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
        initRecyclerView();
        loadAllTransactions();
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
                .subscribe(this::handleTransactionLoaded);

        this.subscriptions.add(sub);
    }

    private void handleTransactionLoaded(final PendingTransaction transaction) {
        this.adapter.addTransaction(transaction);
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
