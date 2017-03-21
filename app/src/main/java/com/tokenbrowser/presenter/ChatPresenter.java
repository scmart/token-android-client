package com.tokenbrowser.presenter;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.PathInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.model.local.ActivityResultHolder;
import com.tokenbrowser.model.local.Conversation;
import com.tokenbrowser.model.local.Review;
import com.tokenbrowser.model.local.SofaMessage;
import com.tokenbrowser.model.local.User;
import com.tokenbrowser.model.sofa.Command;
import com.tokenbrowser.model.sofa.Control;
import com.tokenbrowser.model.sofa.Message;
import com.tokenbrowser.model.sofa.PaymentRequest;
import com.tokenbrowser.model.sofa.SofaAdapters;
import com.tokenbrowser.token.BuildConfig;
import com.tokenbrowser.token.R;
import com.tokenbrowser.util.FileUtil;
import com.tokenbrowser.util.KeyboardUtil;
import com.tokenbrowser.util.LogUtil;
import com.tokenbrowser.util.OnSingleClickListener;
import com.tokenbrowser.util.PaymentType;
import com.tokenbrowser.util.SoundManager;
import com.tokenbrowser.view.Animation.SlideUpAnimator;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.AmountActivity;
import com.tokenbrowser.view.activity.ChatActivity;
import com.tokenbrowser.view.activity.FullscreenImageActivity;
import com.tokenbrowser.view.activity.ImageConfirmationActivity;
import com.tokenbrowser.view.activity.ViewUserActivity;
import com.tokenbrowser.view.adapter.MessageAdapter;
import com.tokenbrowser.view.custom.SpeedyLinearLayoutManager;
import com.tokenbrowser.view.fragment.DialogFragment.ChooserDialog;
import com.tokenbrowser.view.fragment.DialogFragment.RateDialog;
import com.tokenbrowser.view.notification.ChatNotificationManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subscriptions.CompositeSubscription;


public final class ChatPresenter implements
        Presenter<ChatActivity>,
        RateDialog.OnRateDialogClickListener {

    private static final int REQUEST_RESULT_CODE = 1;
    private static final int PAY_RESULT_CODE = 2;
    private static final int PICK_IMAGE = 3;
    private static final int CAPTURE_IMAGE = 4;
    private static final int CAMERA_PERMISSION = 5;
    private static final int CONFIRM_IMAGE = 6;
    private static final int READ_EXTERNAL_STORAGE_PERMISSION = 7;

    private static final String CAPTURE_FILENAME = "caputureImageFilename";

    private ChatActivity activity;
    private MessageAdapter messageAdapter;
    private User remoteUser;
    private SpeedyLinearLayoutManager layoutManager;
    private SofaAdapters adapters;
    private HDWallet userWallet;
    private CompositeSubscription subscriptions;
    private Dialog notEnoughFundsDialog;
    private boolean firstViewAttachment = true;
    private int lastVisibleMessagePosition;
    private Pair<PublishSubject<SofaMessage>, PublishSubject<SofaMessage>> chatObservables;
    private ReplaySubject<SofaMessage> outgoingMessageQueue;
    private String captureImageFilename;

    @Override
    public void onViewAttached(final ChatActivity activity) {
        this.activity = activity;

        if (this.firstViewAttachment) {
            this.firstViewAttachment = false;
            initLongLivingObjects();
        }
        initShortLivingObjects();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
        this.outgoingMessageQueue = ReplaySubject.create();
        initMessageAdapter();
    }

    private void initMessageAdapter() {
        this.adapters = new SofaAdapters();
        this.messageAdapter = new MessageAdapter()
                .addOnPaymentRequestApproveListener(message -> updatePaymentRequestState(message, PaymentRequest.ACCEPTED))
                .addOnPaymentRequestRejectListener(message -> updatePaymentRequestState(message, PaymentRequest.REJECTED))
                .addOnUsernameClickListener(this::searchForUsername)
                .addOnImageClickListener(this::handleImageClicked);
    }

    private void searchForUsername(final String username) {
        final Subscription sub =
                BaseApplication
                        .get()
                        .getTokenManager()
                        .getUserManager()
                        .searchOnlineUsers(username)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                users -> handleSearchResult(username, users),
                                e -> LogUtil.e(getClass(), e.toString()));

        this.subscriptions.add(sub);
    }

    private void handleImageClicked(final String filePath) {
        final Intent intent = new Intent(this.activity, FullscreenImageActivity.class)
                .putExtra(FullscreenImageActivity.FILE_PATH, filePath);
        this.activity.startActivity(intent);
    }

    private void updatePaymentRequestState(
            final SofaMessage existingMessage,
            final @PaymentRequest.State int newState) {
        BaseApplication
                .get()
                .getTokenManager()
                .getTransactionManager()
                .updatePaymentRequestState(this.remoteUser, existingMessage, newState);
    }

    private void handleSearchResult(final String searchedForUsername, final List<User> userResult) {
        if (this.activity == null) {
            return;
        }

        final boolean usersFound = userResult.size() > 0;
        if (!usersFound) {
            Toast.makeText(this.activity, this.activity.getString(R.string.username_search_response_no_match), Toast.LENGTH_SHORT).show();
            return;
        }

        final User user = userResult.get(0);
        final boolean isSameUser = user.getUsernameForEditing().equals(searchedForUsername);

        if (!isSameUser) {
            Toast.makeText(this.activity, this.activity.getString(R.string.username_search_response_no_match), Toast.LENGTH_SHORT).show();
            return;
        }

        viewProfile(user.getTokenId());
    }

    private void handleUpdatedMessage(final SofaMessage sofaMessage) {
        this.messageAdapter.updateMessage(sofaMessage);
    }

    private void initSubscribers() {
        final Subscription walletSub = BaseApplication.get()
                .getTokenManager()
                .getWallet()
                .subscribeOn(Schedulers.io())
                .subscribe(wallet -> this.userWallet = wallet);

        this.subscriptions.add(walletSub);

        final Subscription pendingTransactionSub = BaseApplication
                .get()
                .getTokenManager()
                .getTransactionManager()
                .getPendingTransactionObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pendingTransaction -> handleUpdatedMessage(pendingTransaction.getSofaMessage()));

        this.subscriptions.add(pendingTransactionSub);

        this.activity.getBinding().userInput.setOnEditorActionListener((v, actionId, event) -> {
            if (event != null && event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }

            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendButtonClicked.onSingleClick(v);
                return true;
            }
            return false;
        });
    }

    private void initShortLivingObjects() {
        initSubscribers();
        initLayoutManager();
        initAdapterAnimation();
        initRecyclerView();
        initButtons();
        initControlView();
        processIntentData();
        initLoadingSpinner(this.remoteUser);
    }

    private void initLayoutManager() {
        this.layoutManager = new SpeedyLinearLayoutManager(this.activity);
        this.activity.getBinding().messagesList.setLayoutManager(this.layoutManager);
    }

    private void initAdapterAnimation() {
        final SlideUpAnimator anim;
        if (Build.VERSION.SDK_INT >= 21) {
            anim = new SlideUpAnimator(new PathInterpolator(0.33f, 0.78f, 0.3f, 1));
        } else {
            anim = new SlideUpAnimator(new DecelerateInterpolator());
        }
        anim.setAddDuration(400);
        this.activity.getBinding().messagesList.setItemAnimator(anim);
    }

    private void initRecyclerView() {
        this.messageAdapter.notifyDataSetChanged();
        this.activity.getBinding().messagesList.setAdapter(this.messageAdapter);

        // Hack to scroll to bottom when keyboard rendered
        this.activity.getBinding().messagesList.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                activity.getBinding().messagesList.postDelayed(() -> activity.getBinding().messagesList.smoothScrollToPosition(messageAdapter.getItemCount() - 1), 100);
            }
        });

        this.activity.getBinding().messagesList.getLayoutManager().scrollToPosition(this.lastVisibleMessagePosition);
        updateEmptyState();
    }

    private void updateEmptyState() {
        // Hide empty state if we have some content
        final boolean showingEmptyState = this.activity.getBinding().emptyStateSwitcher.getCurrentView().getId() == this.activity.getBinding().emptyState.getId();
        final boolean shouldShowEmptyState = this.messageAdapter.getItemCount() == 0;

        if (shouldShowEmptyState && !showingEmptyState) {
            this.activity.getBinding().emptyStateSwitcher.showPrevious();
        } else if (!shouldShowEmptyState && showingEmptyState) {
            this.activity.getBinding().emptyStateSwitcher.showNext();
        }
    }

    private void initButtons() {
        this.activity.getBinding().sendButton.setOnClickListener(this.sendButtonClicked);
        this.activity.getBinding().balanceBar.setOnRequestClicked(this.requestButtonClicked);
        this.activity.getBinding().balanceBar.setOnPayClicked(this.payButtonClicked);
        this.activity.getBinding().controlView.setOnControlClickedListener(this::handleControlClicked);
        this.activity.getBinding().addButton.setOnClickListener(this::handleAddButtonClicked);
    }

    private void handleAddButtonClicked(final View v) {
        final ChooserDialog dialog = ChooserDialog.newInstance();
        dialog.setOnChooserClickListener(new ChooserDialog.OnChooserClickListener() {
            @Override
            public void captureImageClicked() {
                checkCameraPermission();
            }

            @Override
            public void importImageFromGalleryClicked() {
                checkExternalStoragePermission();
            }
        });
        dialog.show(this.activity.getSupportFragmentManager(), ChooserDialog.TAG);
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this.activity,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this.activity,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION);
        } else {
            startCameraActivity();
        }
    }

    private void startCameraActivity() {
        final Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(this.activity.getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = new FileUtil().createImageFileWithRandomName(this.activity);
                this.captureImageFilename = photoFile.getName();
            } catch (IOException e) {
                LogUtil.e(getClass(), "Error during creating image file " + e.getMessage());
            }
            if (photoFile != null) {
                final Uri photoURI = FileProvider.getUriForFile(
                        BaseApplication.get(),
                        BuildConfig.APPLICATION_ID + ".photos",
                        photoFile);
                grantUriPermission(cameraIntent, photoURI);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                this.activity.startActivityForResult(cameraIntent, CAPTURE_IMAGE);
            }
        }
    }

    private void grantUriPermission(final Intent intent, final Uri uri) {
        if (Build.VERSION.SDK_INT >= 21) return;
        final PackageManager pm = this.activity.getPackageManager();
        final String packageName = intent.resolveActivity(pm).getPackageName();
        this.activity.grantUriPermission(
                packageName,
                uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        | Intent.FLAG_GRANT_READ_URI_PERMISSION
        );
    }

    private void checkExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this.activity,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this.activity,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_PERMISSION);
        } else {
            startGalleryActivity();
        }
    }

    private void startGalleryActivity() {
        final Intent pickPictureIntent = new Intent()
                .setType("image/*")
                .setAction(Intent.ACTION_GET_CONTENT);

        if (pickPictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            this.activity.startActivityForResult(Intent.createChooser(
                    pickPictureIntent,
                    BaseApplication.get().getString(R.string.select_picture)), PICK_IMAGE);
        }
    }

    private final OnSingleClickListener sendButtonClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            if (userInputInvalid()) {
                return;
            }

            final String userInput = activity.getBinding().userInput.getText().toString();
            final Message sofaMessage = new Message().setBody(userInput);
            final String messageBody = adapters.toJson(sofaMessage);
            final SofaMessage message = new SofaMessage().makeNew(true, messageBody);
            outgoingMessageQueue.onNext(message);

            activity.getBinding().userInput.setText(null);
        }

        private boolean userInputInvalid() {
            return activity.getBinding().userInput.getText().toString().trim().length() == 0;
        }
    };

    private final OnSingleClickListener requestButtonClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            final Intent intent = new Intent(activity, AmountActivity.class)
                    .putExtra(AmountActivity.VIEW_TYPE, PaymentType.TYPE_REQUEST);
            activity.startActivityForResult(intent, REQUEST_RESULT_CODE);
        }
    };

    private final OnSingleClickListener payButtonClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            final Intent intent = new Intent(activity, AmountActivity.class)
                    .putExtra(AmountActivity.VIEW_TYPE, PaymentType.TYPE_REQUEST);
            activity.startActivityForResult(intent, PAY_RESULT_CODE);
        }
    };

    private void handleControlClicked(final Control control) {
        this.activity.getBinding().controlView.hideView();
        removePadding();
        sendCommandMessage(control);
    }

    private void removePadding() {
        final int paddingRight = this.activity.getBinding().messagesList.getPaddingRight();
        final int paddingLeft = this.activity.getBinding().messagesList.getPaddingLeft();
        final int paddingBottom = this.activity.getResources().getDimensionPixelSize(R.dimen.message_list_bottom_padding);
        this.activity.getBinding().messagesList.setPadding(paddingLeft, 0 , paddingRight, paddingBottom);
    }

    private void sendCommandMessage(final Control control) {
        final Command command = new Command()
                .setBody(control.getLabel())
                .setValue(control.getValue());
        final String commandPayload = adapters.toJson(command);
        final SofaMessage sofaMessage = new SofaMessage().makeNew(true, commandPayload);
        this.outgoingMessageQueue.onNext(sofaMessage);
    }

    private void initControlView() {
        this.activity.getBinding().controlView.setOnSizeChangedListener(this::setPadding);
    }

    private void setPadding(final int height) {
        final int paddingRight = this.activity.getBinding().messagesList.getPaddingRight();
        final int paddingLeft = this.activity.getBinding().messagesList.getPaddingLeft();
        this.activity.getBinding().messagesList.setPadding(paddingLeft, 0 , paddingRight, height);
        this.activity.getBinding().messagesList.scrollToPosition(this.messageAdapter.getItemCount() - 1);
    }

    private void processIntentData() {
        if (this.remoteUser == null) {
            final String remoteUserAddress = this.activity.getIntent().getStringExtra(ChatActivity.EXTRA__REMOTE_USER_ADDRESS);
            fetchUserFromAddress(remoteUserAddress);
            return;
        }

        updateUiFromRemoteUser();
        processPaymentFromIntent();
    }

    private void fetchUserFromAddress(final String remoteUserAddress) {
        final Subscription sub =
                BaseApplication
                        .get()
                        .getTokenManager()
                        .getUserManager()
                        .getUserFromAddress(remoteUserAddress)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::handleUserLoaded, this::handleUserFetchFailed);

        this.subscriptions.add(sub);
    }

    private void handleUserLoaded(final User user) {
        this.remoteUser = user;
        if (this.remoteUser != null) {
            processIntentData();
            attachOutgoingQueueSubscriber();
        }
    }

    private void attachOutgoingQueueSubscriber() {
        final Subscription subscription =
                this.outgoingMessageQueue
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(outgoingSofaMessage ->
                        BaseApplication
                                .get()
                                .getTokenManager()
                                .getSofaMessageManager()
                                .sendAndSaveMessage(this.remoteUser, outgoingSofaMessage)
                );

        this.subscriptions.add(subscription);
    }

    private void handleUserFetchFailed(final Throwable throwable) {
        Toast.makeText(BaseApplication.get(), R.string.error__app_loading, Toast.LENGTH_LONG).show();
        if (this.activity != null) {
            this.activity.finish();
        }
    }

    private void updateUiFromRemoteUser() {
        initToolbar(this.remoteUser);
        initChatMessageStore(this.remoteUser);
        initLoadingSpinner(this.remoteUser);
    }

    private void processPaymentFromIntent() {
        if (this.remoteUser == null) {
            return;
        }

        final String value = this.activity.getIntent().getStringExtra(ChatActivity.EXTRA__ETH_AMOUNT);
        final int paymentAction = this.activity.getIntent().getIntExtra(ChatActivity.EXTRA__PAYMENT_ACTION, 0);

        this.activity.getIntent().removeExtra(ChatActivity.EXTRA__ETH_AMOUNT);
        this.activity.getIntent().removeExtra(ChatActivity.EXTRA__PAYMENT_ACTION);

        if (value == null || paymentAction == 0) {
            return;
        }

        if (paymentAction == PaymentType.TYPE_SEND) {
            sendPaymentWithValue(value);
        } else if (paymentAction == PaymentType.TYPE_REQUEST) {
            sendPaymentRequestWithValue(value);
        }
    }

    private void sendPaymentWithValue(final String value) {
        BaseApplication.get()
                .getTokenManager()
                .getTransactionManager()
                .sendPayment(remoteUser, value);
    }

    private void sendPaymentRequestWithValue(final String value) {
        new PaymentRequest()
                .setDestinationAddress(userWallet.getPaymentAddress())
                .setValue(value)
                .generateLocalPrice()
                .subscribe((request) -> {
                    final String messageBody = this.adapters.toJson(request);
                    final SofaMessage message = new SofaMessage().makeNew(true, messageBody);
                    this.outgoingMessageQueue.onNext(message);
                });

    }

    private void initLoadingSpinner(final User remoteUser) {
        if (this.activity == null) return;
        this.activity.getBinding().loadingViewContainer.setVisibility(remoteUser == null ? View.VISIBLE : View.GONE);
        if (remoteUser == null) {
            final Animation rotateAnimation = AnimationUtils.loadAnimation(this.activity, R.anim.rotate);
            this.activity.getBinding().loadingView.startAnimation(rotateAnimation);
        } else {
            this.activity.getBinding().loadingView.clearAnimation();
        }
    }

    private void initToolbar(final User remoteUser) {
        this.activity.getBinding().title.setText(remoteUser.getDisplayName());
        this.activity.getBinding().closeButton.setOnClickListener(this::handleBackButtonClicked);
        this.activity.setSupportActionBar(this.activity.getBinding().toolbar);
        this.activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        Glide.with(this.activity.getBinding().avatar.getContext())
                .load(remoteUser.getAvatar())
                .into(this.activity.getBinding().avatar);
    }

    private void handleBackButtonClicked(final View v) {
        hideKeyboard();
        this.activity.onBackPressed();
    }

    private void initChatMessageStore(final User remoteUser) {
        ChatNotificationManager.suppressNotificationsForConversation(remoteUser.getTokenId());

        this.chatObservables =
                BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager()
                .registerForConversationChanges(remoteUser.getTokenId());

        final Subscription subConversationLoaded = BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager()
                .loadConversation(remoteUser.getTokenId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleConversationLoaded);

        final Subscription subNewMessage = chatObservables.first
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleNewMessage);

        final Subscription subUpdateMessage = chatObservables.second
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleUpdatedMessage);

        this.subscriptions.addAll(subConversationLoaded, subNewMessage, subUpdateMessage);
    }

    private void handleConversationLoaded(final Conversation conversation) {
        if (conversation == null) {
            return;
        }

        final List<SofaMessage> messages = conversation.getAllMessages();
        if (messages.size() > 0) {
            this.messageAdapter.addMessages(messages);
            forceScrollToBottom();
            updateEmptyState();

            final SofaMessage lastSofaMessage = messages.get(messages.size() - 1);
            setControlView(lastSofaMessage);
        }
    }

    private void handleNewMessage(final SofaMessage sofaMessage) {
        setControlView(sofaMessage);
        this.messageAdapter.updateMessage(sofaMessage);
        updateEmptyState();
        tryScrollToBottom(true);
        playNewMessageSound(sofaMessage.isSentByLocal());
        handleKeyboardVisibility(sofaMessage);
    }

    private void tryScrollToBottom(final boolean animate) {
        if (this.activity == null || this.layoutManager == null || this.messageAdapter.getItemCount() == 0) {
            return;
        }

        // Only animate if we're already near the bottom
        if (this.layoutManager.findLastVisibleItemPosition() < this.messageAdapter.getItemCount() - 3) {
            return;
        }

        if (animate) {
            this.activity.getBinding().messagesList.smoothScrollToPosition(this.messageAdapter.getItemCount() - 1);
        } else {
            forceScrollToBottom();
        }
    }

    private void playNewMessageSound(final boolean sentByLocal) {
        if (sentByLocal) {
            SoundManager.getInstance().playSound(SoundManager.SEND_MESSAGE);
        } else {
            SoundManager.getInstance().playSound(SoundManager.RECEIVE_MESSAGE);
        }
    }

    private void handleKeyboardVisibility(final SofaMessage sofaMessage) {
        final boolean viewIsNull = this.activity == null || this.activity.getBinding().userInput == null;
        if (viewIsNull || sofaMessage.isSentByLocal()) {
            return;
        }

        try {
            final Message message = this.adapters.messageFrom(sofaMessage.getPayload());
            if (message.shouldHideKeyboard()) {
                hideKeyboard();
            }

        } catch (IOException e) {
            LogUtil.e(getClass(), "Error during handling visibility of keyboard");
        }
    }

    private void hideKeyboard() {
        KeyboardUtil.hideKeyboard(this.activity.getBinding().userInput);
    }

    private void setControlView(final SofaMessage sofaMessage) {
        if (sofaMessage == null || this.activity == null) {
            return;
        }

        try {
            final Message message = adapters.messageFrom(sofaMessage.getPayload());
            final boolean notNullAndNotZero = message.getControls() != null && message.getControls().size() > 0;
            this.activity.getBinding().controlView.hideView();

            if (notNullAndNotZero) {
                this.activity.getBinding().controlView.showControls(message.getControls());
            } else {
                removePadding();
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void forceScrollToBottom() {
        this.activity.getBinding().messagesList.scrollToPosition(this.messageAdapter.getItemCount() - 1);
    }

    public boolean handleActivityResult(final ActivityResultHolder resultHolder) {
        if (resultHolder.getResultCode() != Activity.RESULT_OK || this.activity == null) {
            return false;
        }

        if (resultHolder.getRequestCode() == REQUEST_RESULT_CODE) {
            final String value = resultHolder.getIntent().getStringExtra(AmountPresenter.INTENT_EXTRA__ETH_AMOUNT);
            sendPaymentRequestWithValue(value);
        } else if(resultHolder.getRequestCode() == PAY_RESULT_CODE) {
            final String value = resultHolder.getIntent().getStringExtra(AmountPresenter.INTENT_EXTRA__ETH_AMOUNT);
            sendPaymentWithValue(value);
        } else if (resultHolder.getRequestCode() == PICK_IMAGE) {
            try {
                handleGalleryResult(resultHolder);
            } catch (IOException e) {
                LogUtil.e(getClass(), "Error during image saving " + e.getMessage());
                return false;
            }
        } else if (resultHolder.getRequestCode() == CAPTURE_IMAGE) {
            try {
                handleCameraResult();
            } catch (FileNotFoundException e) {
                LogUtil.e(getClass(), "Error during sending camera image " + e.getMessage());
                return false;
            }
        } else if (resultHolder.getRequestCode() == CONFIRM_IMAGE) {
            try {
                handleConfirmationResult(resultHolder);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void handlePermission(final int requestCode,
                                  @NonNull final String permissions[],
                                  @NonNull final int[] grantResults) {
        if (grantResults.length == 0) return;

        switch (requestCode) {
            case CAMERA_PERMISSION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCameraActivity();
                }
                break;
            }
            case READ_EXTERNAL_STORAGE_PERMISSION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startGalleryActivity();
                }
                break;
            }
        }
    }

    private void handleGalleryResult(final ActivityResultHolder resultHolder) throws IOException {
        final Uri uri = resultHolder.getIntent().getData();
        final Intent confirmationIntent = new Intent(this.activity, ImageConfirmationActivity.class)
                .putExtra(ImageConfirmationActivity.FILE_URI, uri);
        this.activity.startActivityForResult(confirmationIntent, CONFIRM_IMAGE);
    }

    private void handleConfirmationResult(final ActivityResultHolder resultHolder) throws FileNotFoundException {
        final String filePath = resultHolder.getIntent().getStringExtra(ImageConfirmationActivity.FILE_PATH);

        if (filePath == null) {
            startGalleryActivity();
            return;
        }

        final File file = new File(filePath);
        final FileUtil fileUtil = new FileUtil();
        fileUtil.compressImage(FileUtil.MAX_SIZE, file);
        sendMediaMessage(file.getAbsolutePath());
    }

    private void handleCameraResult() throws FileNotFoundException {
        final File file = new File(this.activity.getFilesDir(), this.captureImageFilename);
        final FileUtil fileUtil = new FileUtil();
        fileUtil.compressImage(FileUtil.MAX_SIZE, file);
        sendMediaMessage(file.getAbsolutePath());
    }

    private void sendMediaMessage(final String filePath) {
        final Message message = new Message();
        final String messageBody = this.adapters.toJson(message);
        final SofaMessage sofaMessage = new SofaMessage()
                .makeNew(true, messageBody)
                .setAttachmentFilePath(filePath);
        this.outgoingMessageQueue.onNext(sofaMessage);
    }

    private void showNotEnoughFundsDialog() {
        if (this.activity == null) {
            return;
        }

        this.notEnoughFundsDialog = new AlertDialog.Builder(this.activity)
                .setTitle(R.string.not_enough_funds_title)
                .setMessage(R.string.not_enough_funds_message)
                .setPositiveButton(R.string.got_it, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }

    public void handleActionMenuClicked(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rate: {
                rateUser();
                break;
            }
            case R.id.view_profile: {
                viewProfile(this.remoteUser.getTokenId());
                break;
            }
            default: {
                LogUtil.d(getClass(), "Not valid menu item");
            }
        }
    }

    private void viewProfile(final String ownerAddress) {
        if (this.remoteUser == null) {
            return;
        }

        final Intent intent = new Intent(this.activity, ViewUserActivity.class)
                .putExtra(ViewUserActivity.EXTRA__USER_ADDRESS, ownerAddress);
        this.activity.startActivity(intent);
    }

    private void rateUser() {
        if (this.remoteUser == null) {
            return;
        }

        final RateDialog dialog = RateDialog
                .newInstance(this.remoteUser.getUsername());
        dialog.setOnRateDialogClickListener(this);
        dialog.show(this.activity.getSupportFragmentManager(), RateDialog.TAG);
    }

    @Override
    public void onRateClicked(final int rating, final String reviewText) {
        final Review review = new Review()
                .setRating(rating)
                .setReview(reviewText)
                .setReviewee(this.remoteUser.getTokenId());

        final Subscription sub = BaseApplication
                .get()
                .getTokenManager()
                .getUserManager()
                .getTimestamp()
                .flatMap(serverTime -> BaseApplication.get()
                        .getTokenManager()
                        .getReputationManager()
                        .submitReview(review, serverTime.get()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response ->
                        Toast.makeText(this.activity, "Review submitted", Toast.LENGTH_SHORT).show(),
                        t -> LogUtil.e(getClass(), "Error when sending review " + t.getMessage()));

        this.subscriptions.add(sub);
    }

    @Override
    public void onViewDetached() {
        if (this.notEnoughFundsDialog != null) {
            this.notEnoughFundsDialog.dismiss();
        }
        this.lastVisibleMessagePosition = this.layoutManager.findLastVisibleItemPosition();
        this.subscriptions.clear();
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.messageAdapter = null;
        stopListeningForConversationChanges();
        ChatNotificationManager.stopNotificationSuppression();
        this.subscriptions = null;
        this.chatObservables = null;
        this.outgoingMessageQueue = null;
    }

    private void stopListeningForConversationChanges() {
        BaseApplication
                .get()
                .getTokenManager()
                .getSofaMessageManager()
                .stopListeningForConversationChanges();
    }

    public void onSaveInstanceState(final Bundle outState) {
        outState.putString(CAPTURE_FILENAME, this.captureImageFilename);
    }

    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        this.captureImageFilename = savedInstanceState.getString(CAPTURE_FILENAME);
    }

}