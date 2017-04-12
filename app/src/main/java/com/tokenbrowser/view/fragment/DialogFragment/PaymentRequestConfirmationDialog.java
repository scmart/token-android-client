package com.tokenbrowser.view.fragment.DialogFragment;

import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.tokenbrowser.R;
import com.tokenbrowser.crypto.util.TypeConverter;
import com.tokenbrowser.databinding.FragmentPaymentRequestConfirmationBinding;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.util.EthUtil;
import com.tokenbrowser.util.ImageUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.PaymentType;
import com.tokenbrowser.view.BaseApplication;

import java.math.BigDecimal;
import java.math.BigInteger;

import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class PaymentRequestConfirmationDialog extends DialogFragment {

    public static final String TAG = "PaymentRequestConfirmationDialog";
    public static final String TOKEN_ID = "scan_result";
    public static final String ETH_AMOUNT = "eth_amount";
    public static final String PAYMENT_TYPE = "payment_type";
    public static final String MEMO = "memo";

    private FragmentPaymentRequestConfirmationBinding binding;
    private OnActionClickListener listener;
    private CompositeSubscription subscriptions;

    private String encodedEthAmount;
    private String userAddress;
    private @PaymentType.Type int paymentType;
    private String memo;

    public interface OnActionClickListener {
        void onPaymentApproved(final String userAddress);
        void onPaymentRejected();
    }

    public void setOnActionClickedListener(final OnActionClickListener listener) {
        this.listener = listener;
    }

    public static PaymentRequestConfirmationDialog newInstance(final String tokenId,
                                                               final String value,
                                                               final @PaymentType.Type int paymentType,
                                                               final String memo) {
        final Bundle bundle = new Bundle();
        bundle.putString(TOKEN_ID, tokenId);
        bundle.putString(ETH_AMOUNT, value);
        bundle.putInt(PAYMENT_TYPE, paymentType);
        bundle.putString(MEMO, memo);
        final PaymentRequestConfirmationDialog fragment = new PaymentRequestConfirmationDialog();
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@NonNull Bundle state) {
        final Dialog dialog = super.onCreateDialog(state);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment__payment_request_confirmation, container, false);
        initShortLivingObjects();
        return this.binding.getRoot();
    }

    private void initShortLivingObjects() {
        this.subscriptions = new CompositeSubscription();
        initClickListeners();
        getBundleData();
        fetchUser();
    }

    private void initClickListeners() {
        this.binding.approve.setOnClickListener(this::handleApprovedClicked);
        this.binding.reject.setOnClickListener(this::handleRejectClicked);
    }

    @SuppressWarnings("WrongConstant")
    private void getBundleData() {
        this.userAddress = this.getArguments().getString(TOKEN_ID);
        this.encodedEthAmount = this.getArguments().getString(ETH_AMOUNT);
        this.paymentType = this.getArguments().getInt(PAYMENT_TYPE);
        this.memo = this.getArguments().getString(MEMO);
    }

    private void fetchUser() {
        final Subscription sub = BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .getUserFromAddress(this.userAddress)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::updateView,
                        this::handleUserError
                );

        this.subscriptions.add(sub);
    }

    private void handleUserError(final Throwable throwable) {
        LogUtil.e(getClass(), throwable.toString());
    }

    private void updateView(final User user) {
        ImageUtil.loadFromNetwork(user.getAvatar(), this.binding.avatar);

        final String title = this.paymentType == PaymentType.TYPE_SEND
                ? this.getString(R.string.confirmation_dialog_title_payment)
                : this.getString(R.string.confirmation_dialog_title_request);
        this.binding.title.setText(title);
        this.binding.displayName.setText(user.getDisplayName());
        this.binding.username.setText(user.getUsername());
        final String reviewCount = BaseApplication.get().getString(R.string.parentheses, user.getReviewCount());
        this.binding.numberOfRatings.setText(reviewCount);
        final double reputationScore = user.getReputationScore() != null
                ? user.getReputationScore()
                : 0;
        this.binding.ratingView.setStars(reputationScore);
        renderMemo();
        renderLocalCurrency();
    }

    private void renderMemo() {
        if (this.memo == null) return;
        this.binding.memo.setVisibility(View.VISIBLE);
        this.binding.memo.setText(this.memo);
    }

    private void renderLocalCurrency() {
        final BigInteger weiAmount = TypeConverter.StringHexToBigInteger(this.encodedEthAmount);
        final BigDecimal ethAmount = EthUtil.weiToEth(weiAmount);

        final Subscription sub =
                convertEthToLocalCurrency(ethAmount)
                .subscribe((localCurrency) -> {
                    final String usdEth = this.getString(R.string.eth_usd, localCurrency, getEthValue());
                    this.binding.ethUsd.setText(usdEth);
                });

        this.subscriptions.add(sub);
    }

    private Single<String> convertEthToLocalCurrency(final BigDecimal ethAmount) {
        return BaseApplication.get()
                .getTokenManager()
                .getBalanceManager()
                .convertEthToLocalCurrencyString(ethAmount);
    }

    private String getEthValue() {
        final BigInteger eth = TypeConverter.StringHexToBigInteger(this.encodedEthAmount);
        return EthUtil.weiAmountToUserVisibleString(eth);
    }

    private void handleApprovedClicked(final View v) {
        this.listener.onPaymentApproved(this.userAddress);
        this.dismiss();
    }

    private void handleRejectClicked(final View v) {
        this.listener.onPaymentRejected();
        this.dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.subscriptions.clear();
    }
}
