package com.bakkenbaeck.token.presenter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.util.SingleSuccessSubscriber;
import com.bakkenbaeck.token.util.FileNames;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.BackupPhraseVerifyActivity;
import com.bakkenbaeck.token.view.activity.MainActivity;

import java.util.Arrays;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BackupPhraseVerifyPresenter implements Presenter<BackupPhraseVerifyActivity> {

    public static final String BACKUP_PHRASE_STATE = "back_phrase_state";

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
        final SharedPreferences prefs = BaseApplication.get().getSharedPreferences(FileNames.BACKUP_PHRASE_STATE, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(BACKUP_PHRASE_STATE, true).apply();

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
