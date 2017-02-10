package com.bakkenbaeck.token.presenter;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.util.TypeConverter;
import com.bakkenbaeck.token.util.EthUtil;
import com.bakkenbaeck.token.util.ViewTypePayment;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.ChooseContactsActivity;

import java.math.BigDecimal;
import java.math.BigInteger;

public class ChooseContactPresenter implements Presenter<ChooseContactsActivity> {

    private ChooseContactsActivity activity;
    private String encodedEthAmount;
    private @ViewTypePayment.ViewType int viewType;

    @Override
    public void onViewAttached(final ChooseContactsActivity view) {
        this.activity = view;

        getIntentData();
        initToolbar();
    }

    @SuppressWarnings("WrongConstant")
    private void getIntentData() {
        this.encodedEthAmount = this.activity.getIntent().getStringExtra(AmountPresenter.INTENT_EXTRA__ETH_AMOUNT);
        this.viewType = this.activity.getIntent().getIntExtra(ChooseContactsActivity.VIEW_TYPE, ViewTypePayment.TYPE_SEND);
    }

    private void initToolbar() {
        final BigInteger weiAmount = TypeConverter.StringHexToBigInteger(this.encodedEthAmount);
        final BigDecimal ethAmount = EthUtil.weiToEth(weiAmount);
        final String localCurrency = BaseApplication.get().getTokenManager().getBalanceManager().convertEthToLocalCurrencyString(ethAmount);
        final String paymentAction = this.viewType == ViewTypePayment.TYPE_SEND
                ? this.activity.getString(R.string.send)
                : this.activity.getString(R.string.request);

        final String title = this.activity.getString(R.string.send_amount, paymentAction, localCurrency);
        this.activity.getBinding().title.setText(title);
        this.activity.getBinding().closeButton.setOnClickListener(view -> this.activity.finish());
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        this.activity = null;
    }
}