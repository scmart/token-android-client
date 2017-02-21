package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.view.View;

import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.BackupPhraseVerifyActivity;
import com.bakkenbaeck.token.view.activity.MainActivity;

import java.util.Arrays;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BackupPhraseVerifyPresenter implements Presenter<BackupPhraseVerifyActivity> {

    private BackupPhraseVerifyActivity activity;

    @Override
    public void onViewAttached(BackupPhraseVerifyActivity view) {
        this.activity = view;
        addBackupPhrase();
        initListeners();
    }

    private void addBackupPhrase() {
        BaseApplication.get()
                .getTokenManager()
                .getWallet()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSuccessSubscriber<HDWallet>() {
                    @Override
                    public void onSuccess(HDWallet hdWallet) {
                        handleBackupPhraseCallback(hdWallet);
                        this.unsubscribe();
                    }
                });
    }

    private void handleBackupPhraseCallback(final HDWallet wallet) {
        final String[] backupPhrase = wallet.getMasterSeed().split(" ");
        this.activity.getBinding().dragAndDropView.setBackupPhrase(Arrays.asList(backupPhrase));
    }

    private void initListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(this::handleCloseButtonClosed);
        this.activity.getBinding().dragAndDropView.setOnFinishedListener(this::handleFinishedListener);
    }

    private void handleFinishedListener() {
        final Intent intent = new Intent(this.activity, MainActivity.class);
        this.activity.startActivity(intent);
    }

    private void handleCloseButtonClosed(final View v) {
        this.activity.finish();
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
