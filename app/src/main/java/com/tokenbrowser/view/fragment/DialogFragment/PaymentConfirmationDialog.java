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

package com.tokenbrowser.view.fragment.DialogFragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.FragmentPaymentRequestConfirmationBinding;
import com.tokenbrowser.model.sofa.Payment;
import com.tokenbrowser.presenter.PaymentRequestConfirmPresenter;
import com.tokenbrowser.presenter.factory.PaymentRequestConfirmPresenterFactory;
import com.tokenbrowser.presenter.factory.PresenterFactory;
import com.tokenbrowser.util.PaymentConfirmViewType;
import com.tokenbrowser.util.PaymentType;

public class PaymentConfirmationDialog extends BaseDialogFragment<PaymentRequestConfirmPresenter, PaymentConfirmationDialog> {

    public static final String TAG = "PaymentConfirmationDialog";
    public static final String TOKEN_ID = "token_id";
    public static final String PAYMENT_ADDRESS = "payment_address";
    public static final String ETH_AMOUNT = "eth_amount";
    public static final String MEMO = "memo";
    public static final String PAYMENT_TYPE = "payment_type";
    public static final String VIEW_TYPE = "view_type";

    private FragmentPaymentRequestConfirmationBinding binding;
    private OnPaymentConfirmationListener listener;

    public static PaymentConfirmationDialog newInstanceTokenPayment(final String tokenId,
                                                                    final String value,
                                                                    final String memo) {
        final Bundle bundle = new Bundle();
        bundle.putString(TOKEN_ID, tokenId);
        bundle.putString(ETH_AMOUNT, value);
        bundle.putString(MEMO, memo);
        bundle.putInt(PAYMENT_TYPE, PaymentType.TYPE_SEND);
        bundle.putInt(VIEW_TYPE, PaymentConfirmViewType.TOKEN);
        final PaymentConfirmationDialog fragment = new PaymentConfirmationDialog();
        fragment.setArguments(bundle);
        return fragment;
    }

    public static PaymentConfirmationDialog newInstanceExternalPayment(final String paymentAddress,
                                                                       final String value,
                                                                       final String memo) {
        final Bundle bundle = new Bundle();
        bundle.putString(PAYMENT_ADDRESS, paymentAddress);
        bundle.putString(ETH_AMOUNT, value);
        bundle.putString(MEMO, memo);
        bundle.putInt(PAYMENT_TYPE, PaymentType.TYPE_SEND);
        bundle.putInt(VIEW_TYPE, PaymentConfirmViewType.EXTERNAL);
        final PaymentConfirmationDialog fragment = new PaymentConfirmationDialog();
        fragment.setArguments(bundle);
        return fragment;
    }

    public interface OnPaymentConfirmationListener {
        void onPaymentRejected();
        void onTokenPaymentApproved(final String tokenId, final Payment payment);
        void onExternalPaymentApproved(final Payment payment);
    }

    public void setOnPaymentConfirmationListener(final OnPaymentConfirmationListener listener) {
        this.listener = listener;
    }

    public OnPaymentConfirmationListener getPaymentConfirmationListener() {
        return this.listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, final @Nullable Bundle inState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment__payment_request_confirmation, container, false);
        return binding.getRoot();
    }

    public FragmentPaymentRequestConfirmationBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<PaymentRequestConfirmPresenter> getPresenterFactory() {
        return new PaymentRequestConfirmPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull PaymentRequestConfirmPresenter presenter) {}

    @Override
    protected int loaderId() {
        return 10;
    }
}
