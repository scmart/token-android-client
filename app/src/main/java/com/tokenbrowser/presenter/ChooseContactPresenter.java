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

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.tokenbrowser.R;
import com.tokenbrowser.crypto.util.TypeConverter;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.util.EthUtil;
import com.tokenbrowser.util.PaymentType;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.ChatActivity;
import com.tokenbrowser.view.activity.ChooseContactsActivity;
import com.tokenbrowser.view.adapter.ContactsAdapter;
import com.tokenbrowser.view.custom.HorizontalLineDivider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class ChooseContactPresenter implements Presenter<ChooseContactsActivity> {

    private @PaymentType.Type
    int paymentType;
    private ChooseContactsActivity activity;
    private String encodedEthAmount;
    private ContactsAdapter adapter;
    private User recipientUser;
    private String localCurrency;
    private boolean firstTimeAttaching = true;
    private CompositeSubscription subscriptions;

    @Override
    public void onViewAttached(final ChooseContactsActivity view) {
        this.activity = view;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }

        getIntentData();
        generateLocalAmount();
        initView();
        loadContacts();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
        this.adapter = new ContactsAdapter()
                .setOnItemClickListener(this::handleItemClicked);
    }

    @SuppressWarnings("WrongConstant")
    private void getIntentData() {
        this.encodedEthAmount = this.activity.getIntent().getStringExtra(AmountPresenter.INTENT_EXTRA__ETH_AMOUNT);
        this.paymentType = this.activity.getIntent().getIntExtra(ChooseContactsActivity.VIEW_TYPE, PaymentType.TYPE_SEND);
    }

    private void generateLocalAmount() {
        final BigInteger weiAmount = TypeConverter.StringHexToBigInteger(this.encodedEthAmount);
        final BigDecimal ethAmount = EthUtil.weiToEth(weiAmount);
        this.subscriptions.add(
                BaseApplication
                .get()
                .getTokenManager()
                .getBalanceManager()
                .convertEthToLocalCurrencyString(ethAmount)
                .subscribe(this::setLocalCurrency)
        );
    }

    private void setLocalCurrency(final String localCurrency) {
        this.localCurrency = localCurrency;
        initToolbar();
        setSendButtonText();
    }

    private void initView() {
        initToolbar();
        initRecyclerView();
        updateEmptyState();
        updateConfirmationButtonState();
        setSendButtonText();
        initSearch();
        initClickListeners();
    }

    private void loadContacts() {
        final Subscription sub = BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .loadAllContacts()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(contacts -> {
                    this.adapter.mapContactsToUsers(contacts);
                    updateEmptyState();
                });

        this.subscriptions.add(sub);
    }

    private void initToolbar() {
        final String paymentAction = this.paymentType == PaymentType.TYPE_SEND
                ? this.activity.getString(R.string.send)
                : this.activity.getString(R.string.request);

        final String title = this.activity.getString(R.string.send_amount, paymentAction, this.localCurrency);
        this.activity.getBinding().title.setText(title);
        this.activity.getBinding().closeButton.setOnClickListener(view -> this.activity.finish());
    }

    private void initRecyclerView() {
        final RecyclerView recyclerView = this.activity.getBinding().contacts;
        recyclerView.setLayoutManager(new LinearLayoutManager(this.activity));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        final int dividerLeftPadding = activity.getResources().getDimensionPixelSize(R.dimen.avatar_size_small)
                + activity.getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        final HorizontalLineDivider lineDivider =
                new HorizontalLineDivider(ContextCompat.getColor(this.activity, R.color.divider))
                        .setLeftPadding(dividerLeftPadding);
        recyclerView.addItemDecoration(lineDivider);
        recyclerView.setAdapter(this.adapter);
    }

    private void updateEmptyState() {
        final boolean showingEmptyState = this.activity.getBinding().emptyStateSwitcher.getCurrentView().getId() == this.activity.getBinding().emptyState.getId();
        final boolean shouldShowEmptyState = this.activity.getBinding().contacts.getAdapter().getItemCount() == 0;

        if (shouldShowEmptyState && !showingEmptyState) {
            this.activity.getBinding().emptyStateSwitcher.showPrevious();
        } else if (!shouldShowEmptyState && showingEmptyState) {
            this.activity.getBinding().emptyStateSwitcher.showNext();
        }
    }

    private void updateConfirmationButtonState() {
        this.activity.getBinding().btnContinue.setEnabled(this.recipientUser != null);

        final int color = this.recipientUser != null
                ? ContextCompat.getColor(this.activity, R.color.colorPrimary)
                : ContextCompat.getColor(this.activity, R.color.textColorSecondary);
        this.activity.getBinding().btnContinue.setTextColor(color);
    }

    private void setSendButtonText() {
        final String btnText = this.activity.getString(R.string.button_send_amount, this.localCurrency);
        this.activity.getBinding().btnContinue.setText(btnText);
    }

    private void initSearch() {
        final Subscription sub = RxTextView
                .textChanges(this.activity.getBinding().recipientUser)
                .skip(1)
                .debounce(400, TimeUnit.MILLISECONDS)
                .map(CharSequence::toString)
                .flatMap(this::searchOfflineUsers)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(users -> {
                    this.adapter.setUsers(users);
                    updateEmptyState();
                });

        this.subscriptions.add(sub);
    }

    private void initClickListeners() {
        this.activity.getBinding().btnContinue.setOnClickListener(view -> handleSendClicked());
    }

    private Observable<List<User>> searchOfflineUsers(final String searchString) {
        return BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .searchOfflineUsers(searchString)
                .toObservable();
    }

    private void handleItemClicked(final User user) {
        this.recipientUser = user;
        this.activity.getBinding().recipientUser.setText(user.getDisplayName());
        updateConfirmationButtonState();
    }

    private void handleSendClicked() {
        final Intent intent = new Intent(activity, ChatActivity.class)
                .putExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS, recipientUser.getTokenId())
                .putExtra(ChatActivity.EXTRA__PAYMENT_ACTION, paymentType)
                .putExtra(ChatActivity.EXTRA__ETH_AMOUNT, encodedEthAmount);

        this.activity.startActivity(intent);
        this.activity.finish();
    }

    @Override
    public void onViewDetached() {
        this.subscriptions.clear();
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.adapter = null;
        this.subscriptions = null;
    }
}