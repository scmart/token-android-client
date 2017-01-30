package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.util.EthUtil;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.AmountActivity;
import com.bakkenbaeck.token.view.adapter.AppListAdapter;
import com.bakkenbaeck.token.view.adapter.listeners.OnItemClickListener;
import com.bakkenbaeck.token.view.fragment.toplevel.HomeFragment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


public class HomePresenter implements Presenter<HomeFragment> {

    private HomeFragment fragment;

    @Override
    public void onViewAttached(HomeFragment view) {
        this.fragment = view;
        init();
    }

    private void init() {
        setBalance();
        assignSubscribers();
        assignClickListeners();
        initView();
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

        setBalance();
        assignSubscribers();
        assignClickListeners();
    }

    private void setBalance() {
        final BigDecimal confirmedBalance = BaseApplication.get().getTokenManager().getBalanceManager().getConfirmedBalanceAsEth();
        final String ethConfirmedFormatted = this.fragment.getString(R.string.eth_balance, EthUtil.ethToEthString(confirmedBalance));
        this.fragment.getBinding().balanceEth.setText(ethConfirmedFormatted);

        final BigDecimal localAmount = BaseApplication.get().getTokenManager().getBalanceManager().getMarketRate("USD", confirmedBalance);
        final String localAmountString = this.fragment.getString(R.string.usd_balance, EthUtil.ethToEthString(localAmount), "USD");
        this.fragment.getBinding().balanceUsd.setText(localAmountString);
    }

    private void assignSubscribers() {

    }

    private void assignClickListeners() {
        this.fragment.getBinding().payMoney.setOnClickListener(this.payClickListener);
        this.fragment.getBinding().requestMoney.setOnClickListener(this.requestClickListener);
        this.fragment.getBinding().addMoney.setOnClickListener(this.addMoneyClickListener);
        this.fragment.getBinding().scanQr.setOnClickListener(this.scanQrClickListener);
    }

    private View.OnClickListener payClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final Intent intent = new Intent(fragment.getActivity(), AmountActivity.class);
            fragment.getActivity().startActivity(intent);
        }
    };

    private View.OnClickListener requestClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

    private View.OnClickListener addMoneyClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

    private View.OnClickListener scanQrClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

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
