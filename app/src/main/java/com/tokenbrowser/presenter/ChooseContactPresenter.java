package com.tokenbrowser.presenter;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.jakewharton.rxbinding.widget.RxTextView;
import com.tokenbrowser.crypto.util.TypeConverter;
import com.tokenbrowser.model.local.ActivityResultHolder;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.token.R;
import com.tokenbrowser.util.EthUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.PaymentType;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.ChatActivity;
import com.tokenbrowser.view.activity.ChooseContactsActivity;
import com.tokenbrowser.view.activity.ScannerActivity;
import com.tokenbrowser.view.adapter.ContactsAdapter;
import com.tokenbrowser.view.custom.HorizontalLineDivider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ChooseContactPresenter implements Presenter<ChooseContactsActivity> {

    private static final int REQUEST_CODE = 1;

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

        this.activity.getBinding().qrScan.setOnClickListener(view -> handleQrScanClicked());
        this.activity.getBinding().btnContinue.setOnClickListener(view -> handleSendClicked());
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

    private void handleQrScanClicked() {
        final Intent intent = new Intent(this.activity, ScannerActivity.class)
                .putExtra(ScannerActivity.RESULT_TYPE, ScannerActivity.CONFIRMATION_REDIRECT)
                .putExtra(ScannerActivity.ETH_AMOUNT, this.encodedEthAmount)
                .putExtra(ScannerActivity.PAYMENT_TYPE, this.paymentType);
        this.activity.startActivityForResult(intent, REQUEST_CODE);
    }

    private void handleSendClicked() {
        final Intent intent = new Intent(activity, ChatActivity.class)
                .putExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS, recipientUser.getOwnerAddress())
                .putExtra(ChatActivity.EXTRA__PAYMENT_ACTION, paymentType)
                .putExtra(ChatActivity.EXTRA__ETH_AMOUNT, encodedEthAmount);

        this.activity.startActivity(intent);
        this.activity.finish();
    }

    public void handleActivityResult(final ActivityResultHolder resultHolder) {
        if (resultHolder.getResultCode() != Activity.RESULT_OK) {
            return;
        }

        if (resultHolder.getRequestCode() == REQUEST_CODE) {
            final String userAddress = resultHolder.getIntent().getStringExtra(ScannerPresenter.USER_ADDRESS);
            fetchUser(userAddress);
        }
    }

    private void fetchUser(final String userAddress) {
       final Subscription sub = BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .getUserFromAddress(userAddress)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleUserLoaded, this::handleErrorResponse);

        subscriptions.add(sub);
    }

    private void handleUserLoaded(final User user) {
        this.recipientUser = user;
        this.activity.getBinding().recipientUser.setText(user.getDisplayName());
        updateConfirmationButtonState();
    }

    private void handleErrorResponse(final Throwable throwable) {
        LogUtil.e(getClass(), "handleErrorResponse " + throwable.getMessage());
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        this.subscriptions.clear();
        this.activity = null;
    }
}