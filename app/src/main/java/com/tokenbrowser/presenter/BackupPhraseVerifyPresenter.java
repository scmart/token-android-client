package com.tokenbrowser.presenter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.util.FileNames;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.BackupPhraseVerifyActivity;
import com.tokenbrowser.view.activity.MainActivity;

import java.util.Arrays;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class BackupPhraseVerifyPresenter implements Presenter<BackupPhraseVerifyActivity> {

    public static final String BACKUP_PHRASE_STATE = "back_phrase_state";

    private BackupPhraseVerifyActivity activity;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(BackupPhraseVerifyActivity view) {
        this.activity = view;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }

        addBackupPhrase();
        initListeners();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
    }

    private void addBackupPhrase() {
        final Subscription sub = BaseApplication.get()
                .getTokenManager()
                .getWallet()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleBackupPhraseCallback);

        this.subscriptions.add(sub);
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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MainActivity.EXTRA__ACTIVE_TAB, 4);
        this.activity.startActivity(intent);
        ActivityCompat.finishAffinity(this.activity);
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
        this.subscriptions.clear();
        this.activity = null;
    }
}
