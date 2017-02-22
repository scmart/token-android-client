package com.bakkenbaeck.token.presenter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Toast;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.local.User;
import com.bakkenbaeck.token.util.ImageUtil;
import com.bakkenbaeck.token.util.OnNextSubscriber;
import com.bakkenbaeck.token.util.SharedPrefsUtil;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.DepositActivity;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DepositPresenter implements Presenter<DepositActivity> {

    private DepositActivity activity;
    private User localUser;

    @Override
    public void onViewAttached(DepositActivity view) {
        this.activity = view;
        attachListeners();
        updateView();
        initClickListeners();
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(this::handleCloseButtonClicked);
        this.activity.getBinding().copyToClipboard.setOnClickListener(this::handleCopyToClipboardClicked);
    }

    private void handleCloseButtonClicked(final View v) {
        this.activity.finish();
    }

    private void handleCopyToClipboardClicked(final View v) {
        final ClipboardManager clipboard = (ClipboardManager) this.activity.getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText(this.activity.getString(R.string.backup_phrase), this.localUser.getOwnerAddress());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this.activity, this.activity.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show();
    }

    private void attachListeners() {
        BaseApplication.get()
                .getTokenManager()
                .getUserManager()
                .getUserObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleUserCallback);
    }

    private OnNextSubscriber<User> handleUserCallback = new OnNextSubscriber<User>() {
        @Override
        public void onNext(User user) {
            localUser = user;
            updateView();
            this.unsubscribe();
        }
    };

    private void updateView() {
        if (this.localUser == null) {
            return;
        }

        this.activity.getBinding().ownerAddress.setText(this.localUser.getOwnerAddress());
        generateQrCode();
    }

    private void generateQrCode() {
        ImageUtil.generateQrCodeForWalletAddress(this.localUser.getOwnerAddress())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this.handleQrCodeGenerated);
    }

    private SingleSuccessSubscriber<Bitmap> handleQrCodeGenerated = new SingleSuccessSubscriber<Bitmap>() {
        @Override
        public void onSuccess(final Bitmap qrBitmap) {
            SharedPrefsUtil.saveQrCode(ImageUtil.compressBitmap(qrBitmap));
            renderQrCode(qrBitmap);
        }
    };

    private void renderQrCode(final Bitmap qrCodeBitmap) {
        if (this.activity == null) {
            return;
        }
        this.activity.getBinding().qrCode.setAlpha(0.0f);
        this.activity.getBinding().qrCode.setImageBitmap(qrCodeBitmap);
        this.activity.getBinding().qrCode.animate().alpha(1f).setDuration(200).start();
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
