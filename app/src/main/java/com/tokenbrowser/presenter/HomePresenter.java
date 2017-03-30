package com.tokenbrowser.presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tokenbrowser.model.local.ActivityResultHolder;
import com.tokenbrowser.model.network.App;
import com.tokenbrowser.model.network.Balance;
import com.tokenbrowser.R;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.OnSingleClickListener;
import com.tokenbrowser.util.PaymentType;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.AmountActivity;
import com.tokenbrowser.view.activity.ChatActivity;
import com.tokenbrowser.view.activity.ChooseContactsActivity;
import com.tokenbrowser.view.activity.DepositActivity;
import com.tokenbrowser.view.activity.ScannerActivity;
import com.tokenbrowser.view.adapter.AppListAdapter;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;
import com.tokenbrowser.view.fragment.toplevel.HomeFragment;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;


public class HomePresenter implements Presenter<HomeFragment> {

    private static final int ETH_REQUEST_CODE = 1;
    private static final int ETH_SEND_CODE = 2;

    private HomeFragment fragment;
    private CompositeSubscription subscriptions;
    private List<App> featuredApps;
    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(HomeFragment view) {
        this.fragment = view;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongTermObjects();
        }
        initShortTermObjects();
        getFeaturedApps();
    }

    private void initLongTermObjects() {
        this.subscriptions = new CompositeSubscription();
    }

    private void attachSubscribers() {
        final Subscription sub = BaseApplication
                .get()
                .getTokenManager()
                .getBalanceManager()
                .getBalanceObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleNewBalance);

        this.subscriptions.add(sub);
    }

    private void handleNewBalance(final Balance balance) {
        refreshBalance(balance);
    }

    private void initShortTermObjects() {
        attachSubscribers();
        assignClickListeners();
        initRecyclerView();
    }

    private void assignClickListeners() {
        this.fragment.getBinding().payMoney.setOnClickListener(this.payClickListener);
        this.fragment.getBinding().requestMoney.setOnClickListener(this.requestClickListener);
        this.fragment.getBinding().addMoney.setOnClickListener(this.addMoneyClickListener);
        this.fragment.getBinding().scanQr.setOnClickListener(this.scanQrClickListener);
    }

    private void initRecyclerView() {
        final RecyclerView appList = this.fragment.getBinding().appList;
        final int numColumns = this.fragment.getContext().getResources().getInteger(R.integer.num_app_columns);
        appList.setNestedScrollingEnabled(false);
        appList.setLayoutManager(new GridLayoutManager(this.fragment.getContext(), numColumns));
        final AppListAdapter adapter = new AppListAdapter(new ArrayList<>());
        adapter.setOnItemClickListener(this.appItemClickListener);
        appList.setAdapter(adapter);
    }

    private void refreshBalance(final Balance balance) {
        if (this.fragment == null || balance == null) {
            return;
        }

        this.fragment.getBinding().balanceEth.setText(balance.getFormattedUnconfirmedBalance());

        final Subscription getLocalBalanceSub =
                balance
                .getFormattedLocalBalance()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(localBalance -> this.fragment.getBinding().balanceUsd.setText(localBalance));
        this.subscriptions.add(getLocalBalanceSub);
    }

    private void getFeaturedApps() {
        if (this.featuredApps != null) {
            addFeaturedAppsData(this.featuredApps);
            return;
        }

        final Subscription sub =
                BaseApplication
                .get()
                .getTokenManager()
                .getAppsManager()
                .getFeaturedApps()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleFeaturedApps, this::handleErrorResponse);

        this.subscriptions.add(sub);
    }

    private void handleErrorResponse(final Throwable throwable) {
        LogUtil.e(getClass(), "Error during featuredApps request " + throwable);
    }

    private void handleFeaturedApps(final List<App> featuredApps) {
        this.featuredApps = featuredApps;
        addFeaturedAppsData(featuredApps);
    }

    private void addFeaturedAppsData(final List<App> apps) {
        final AppListAdapter adapter = (AppListAdapter) this.fragment.getBinding().appList.getAdapter();
        adapter.setApps(apps);
    }

    private OnSingleClickListener payClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            final Intent intent = new Intent(fragment.getContext(), AmountActivity.class)
                    .putExtra(AmountActivity.VIEW_TYPE, PaymentType.TYPE_SEND);
            fragment.startActivityForResult(intent, ETH_SEND_CODE);
        }
    };

    private OnSingleClickListener requestClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            final Intent intent = new Intent(fragment.getContext(), AmountActivity.class)
                    .putExtra(AmountActivity.VIEW_TYPE, PaymentType.TYPE_REQUEST);
            fragment.startActivityForResult(intent, ETH_REQUEST_CODE);
        }
    };

    private OnSingleClickListener addMoneyClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            final Intent intent = new Intent(fragment.getActivity(), DepositActivity.class);
            fragment.getActivity().startActivity(intent);
        }
    };

    private OnSingleClickListener scanQrClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            final Intent intent = new Intent(fragment.getActivity(), ScannerActivity.class);
            intent.putExtra(ScannerActivity.RESULT_TYPE, ScannerActivity.REDIRECT);
            fragment.getActivity().startActivity(intent);
        }
    };

    private OnItemClickListener<App> appItemClickListener = this::handleClickEvent;

    private void handleClickEvent(final App app) {
        final Intent intent = new Intent(this.fragment.getContext(), ChatActivity.class)
                .putExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS, app.getTokenId());
        this.fragment.getContext().startActivity(intent);
    }

    public void handleActivityResult(final ActivityResultHolder resultHolder, final Context context) {
        if (resultHolder.getResultCode() != Activity.RESULT_OK) {
            return;
        }

        final @PaymentType.Type int viewType = resultHolder.getRequestCode() == ETH_SEND_CODE
                ? PaymentType.TYPE_SEND
                : PaymentType.TYPE_REQUEST;

        final String value = resultHolder.getIntent().getStringExtra(AmountPresenter.INTENT_EXTRA__ETH_AMOUNT);
        final Intent intent = new Intent(context, ChooseContactsActivity.class)
                .putExtra(ChooseContactsActivity.VIEW_TYPE, viewType)
                .putExtra(AmountPresenter.INTENT_EXTRA__ETH_AMOUNT, value);
        context.startActivity(intent);
    }

    @Override
    public void onViewDetached() {
        this.subscriptions.clear();
        this.fragment = null;
    }

    @Override
    public void onDestroyed() {
        this.fragment = null;
    }
}