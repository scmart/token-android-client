package com.bakkenbaeck.token.view.fragment.DialogFragment;

import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.util.TypeConverter;
import com.bakkenbaeck.token.databinding.FragmentPaymentRequestConfirmationBinding;
import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.network.IdService;
import com.bakkenbaeck.token.util.EthUtil;
import com.bakkenbaeck.token.util.PaymentType;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bumptech.glide.Glide;

import java.math.BigDecimal;
import java.math.BigInteger;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PaymentRequestConfirmationDialog extends DialogFragment {

    public static final String TAG = "PaymentRequestConfirmationDialog";
    public static final String SCAN_RESULT = "scan_result";
    public static final String ETH_AMOUNT = "eth_amount";
    public static final String PAYMENT_TYPE = "payment_type";

    private FragmentPaymentRequestConfirmationBinding binding;
    private OnActionClickListener listener;
    private Subscription userSubscription;
    private String encodedEthAmount;
    private String userAddress;
    private @PaymentType.Type int paymentType;

    public interface OnActionClickListener {
        void onApproved();
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
        this.userSubscription = IdService
                .getApi()
                .getUser(this.userAddress)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::updateView);
    }

    private void updateView(final User user) {
        Glide.with(this.getContext())
                .load(user.getAvatar())
                .into(this.binding.avatar);

        final String title = this.paymentType == PaymentType.TYPE_SEND
                ? this.getString(R.string.confirmation_dialog_title_payment)
                : this.getString(R.string.confirmation_dialog_title_request);
        this.binding.title.setText(title);
        this.binding.displayName.setText(user.getDisplayName());
        this.binding.username.setText(user.getDisplayName());
        final String usdEth = this.getString(R.string.eth_usd, getLocalCurrency(), getEthValue());
        this.binding.ethUsd.setText(usdEth);
    }

    private String getLocalCurrency() {
        final BigInteger weiAmount = TypeConverter.StringHexToBigInteger(this.encodedEthAmount);
        final BigDecimal ethAmount = EthUtil.weiToEth(weiAmount);
        return BaseApplication.get()
                .getTokenManager()
                .getBalanceManager()
                .convertEthToLocalCurrencyString(ethAmount);
    }

    private String getEthValue() {
        final BigInteger eth = TypeConverter.StringHexToBigInteger(this.encodedEthAmount);
        return EthUtil.weiToEthString(eth);
    }

    private void initClickListeners() {
        this.binding.approve.setOnClickListener(this::handleApprovedClicked);
        this.binding.reject.setOnClickListener(this::handleRejectClicked);
    }

    private void handleApprovedClicked(final View v) {
        this.listener.onApproved();
        this.dismiss();
    }

    private void handleRejectClicked(final View v) {
        this.listener.onRejected();
        this.dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (this.userSubscription != null) {
            this.userSubscription.unsubscribe();
        }
    }
}
