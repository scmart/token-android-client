package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bakkenbaeck.token.model.network.Balance;
import com.bakkenbaeck.token.util.OnSingleClickListener;
import com.bakkenbaeck.token.util.ViewTypePayment;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.AmountActivity;
import com.bakkenbaeck.token.view.activity.ScannerActivity;
import com.bakkenbaeck.token.view.adapter.AppListAdapter;
import com.bakkenbaeck.token.view.adapter.listeners.OnItemClickListener;
import com.bakkenbaeck.token.view.fragment.toplevel.HomeFragment;

import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;


public class HomePresenter implements Presenter<HomeFragment> {

    private static final int ETH_REQUEST_CODE = 1;
    private static final int ETH_SEND_CODE = 2;

    private HomeFragment fragment;
    private boolean firstTimeAttaching = true;
    private Balance balance;

    @Override
    public void onViewAttached(HomeFragment view) {
        this.fragment = view;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongTermObjects();
        }
        initShortTermObjects();
    }

    private void initLongTermObjects() {
        assignSubscribers();
    }

    private void initShortTermObjects() {
        assignClickListeners();
        initView();
        refreshBalance();
    }

    private void initView() {
        final RecyclerView rv = this.fragment.getBinding().appList;
        rv.setLayoutManager(new GridLayoutManager(this.fragment.getContext(), 4));

        final List<String> apps = new ArrayList<>();
        apps.add("Draw something");
        apps.add("Token Portfolio");
        apps.add("Perfect GIF");
        apps.add("Local Trade");
        apps.add("Trade ");
        apps.add("Tickets");

        final AppListAdapter adapter = new AppListAdapter(apps);
        adapter.setOnItemClickListener(this.appItemClickListener);
        rv.setAdapter(adapter);

        assignSubscribers();
        assignClickListeners();
    }

    private void refreshBalance() {
        if (this.fragment == null || this.balance == null) {
            return;
        }

        this.fragment.getBinding().balanceEth.setText(this.balance.getFormattedUnconfirmedBalance());
        this.fragment.getBinding().balanceUsd.setText(this.balance.getFormattedLocalBalance());
    }

    private void assignSubscribers() {
        BaseApplication
                .get()
                .getTokenManager()
                .getBalanceManager()
                .getBalanceObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleNewBalance);
    }

    private void handleNewBalance(final Balance balance) {
        this.balance = balance;
        refreshBalance();
    }

    private void assignClickListeners() {
        this.fragment.getBinding().payMoney.setOnClickListener(this.payClickListener);
        this.fragment.getBinding().requestMoney.setOnClickListener(this.requestClickListener);
        this.fragment.getBinding().addMoney.setOnClickListener(this.addMoneyClickListener);
        this.fragment.getBinding().scanQr.setOnClickListener(this.scanQrClickListener);
    }

    private OnSingleClickListener payClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            final Intent intent = new Intent(fragment.getContext(), AmountActivity.class)
                    .putExtra(AmountActivity.VIEW_TYPE, ViewTypePayment.TYPE_SEND);
            fragment.startActivityForResult(intent, ETH_SEND_CODE);
        }
    };

    private OnSingleClickListener requestClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            final Intent intent = new Intent(fragment.getContext(), AmountActivity.class)
                    .putExtra(AmountActivity.VIEW_TYPE, ViewTypePayment.TYPE_REQUEST);
            fragment.startActivityForResult(intent, ETH_REQUEST_CODE);
        }
    };

    private OnSingleClickListener addMoneyClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {

        }
    };

    private OnSingleClickListener scanQrClickListener = new OnSingleClickListener() {
        @Override
        public void onSingleClick(View v) {
            final Intent intent = new Intent(fragment.getActivity(), ScannerActivity.class);
            fragment.getActivity().startActivity(intent);
        }
    };

    private OnItemClickListener appItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(Object item) {
            final int position = (int) item;
        }
    };

    @Override
    public void onViewDetached() {
        this.fragment = null;
    }

    @Override
    public void onViewDestroyed() {
        this.fragment = null;
    }
}