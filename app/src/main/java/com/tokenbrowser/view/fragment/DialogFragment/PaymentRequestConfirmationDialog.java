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

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class PaymentRequestConfirmationDialog extends DialogFragment {

    public static final String TAG = "PaymentRequestConfirmationDialog";
    public static final String SCAN_RESULT = "scan_result";
    public static final String ETH_AMOUNT = "eth_amount";
    public static final String PAYMENT_TYPE = "payment_type";

    private FragmentPaymentRequestConfirmationBinding binding;
    private OnActionClickListener listener;
    private CompositeSubscription subscriptions;

    private String encodedEthAmount;
    private String userAddress;
    private @PaymentType.Type int paymentType;

    public interface OnActionClickListener {
        void onApproved(final String userAddress);
        void onRejected();
    }

    public void setOnActionClickedListener(final OnActionClickListener listener) {
        this.listener = listener;
    }

    public static PaymentRequestConfirmationDialog newInstance(final String scanResult, final String value, final @PaymentType.Type int paymentType) {
        final Bundle bundle = new Bundle();
        bundle.putString(SCAN_RESULT, scanResult);
        bundle.putString(ETH_AMOUNT, value);
        bundle.putInt(PAYMENT_TYPE, paymentType);
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
        this.subscriptions = new CompositeSubscription();
        getBundleData();
        fetchUser();
        initClickListeners();
        return this.binding.getRoot();
    }

    @SuppressWarnings("WrongConstant")
    private void getBundleData() {
        this.userAddress = this.getArguments().getString(SCAN_RESULT);
        this.encodedEthAmount = this.getArguments().getString(ETH_AMOUNT);
        this.paymentType = this.getArguments().getInt(PAYMENT_TYPE);
    }

    private void fetchUser() {
        this.subscriptions.add(
                BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .getUserFromAddress(this.userAddress)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::updateView,
                        this::handleUserError
                )
        );
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
        this.binding.username.setText(user.getDisplayName());
        final String reviewCount = BaseApplication.get().getString(R.string.parentheses, user.getReviewCount());
        this.binding.numberOfRatings.setText(reviewCount);
        final double reputationScore = user.getReputationScore() != null
                ? user.getReputationScore()
                : 0;
        this.binding.ratingView.setStars(reputationScore);
        renderLocalCurrency();
    }

    private void renderLocalCurrency() {
        final BigInteger weiAmount = TypeConverter.StringHexToBigInteger(this.encodedEthAmount);
        final BigDecimal ethAmount = EthUtil.weiToEth(weiAmount);
        this.subscriptions.add(
                BaseApplication.get()
                .getTokenManager()
                .getBalanceManager()
                .convertEthToLocalCurrencyString(ethAmount)
                .subscribe((localCurrency) -> {
                    final String usdEth = this.getString(R.string.eth_usd, localCurrency, getEthValue());
                    this.binding.ethUsd.setText(usdEth);
                })
        );
    }

    private String getEthValue() {
        final BigInteger eth = TypeConverter.StringHexToBigInteger(this.encodedEthAmount);
        return EthUtil.weiAmountToUserVisibleString(eth);
    }

    private void initClickListeners() {
        this.binding.approve.setOnClickListener(this::handleApprovedClicked);
        this.binding.reject.setOnClickListener(this::handleRejectClicked);
    }

    private void handleApprovedClicked(final View v) {
        this.listener.onApproved(this.userAddress);
        this.dismiss();
    }

    private void handleRejectClicked(final View v) {
        this.listener.onRejected();
        this.dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.subscriptions.clear();
    }
}
