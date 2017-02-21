package com.bakkenbaeck.token.presenter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.crypto.HDWallet;
import com.bakkenbaeck.token.view.BaseApplication;
import com.bakkenbaeck.token.view.activity.BackupPhraseActivity;
import com.bakkenbaeck.token.view.activity.BackupPhraseVerifyActivity;
import com.bakkenbaeck.token.view.adapter.BackupPhraseAdapter;
import com.bakkenbaeck.token.view.custom.SpaceDecoration;
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BackupPhrasePresenter implements Presenter<BackupPhraseActivity> {

    private BackupPhraseActivity activity;
    private Subscription backupPhraseSubscription;

    @Override
    public void onViewAttached(BackupPhraseActivity view) {
        this.activity = view;
        initRecyclerView();
        addBackupPhrase();
        initClickListeners();
    }

    private void initRecyclerView() {
        final RecyclerView backupPhraseList = this.activity.getBinding().backupPhraseList;

        if (backupPhraseList.getAdapter() != null) {
            return;
        }

        final ChipsLayoutManager chipsLayoutManager = ChipsLayoutManager.newBuilder(this.activity)
                .build();
        final int controlSpacing = BaseApplication.get().getResources().getDimensionPixelSize(R.dimen. backup_phrase_spacing);
        backupPhraseList.addItemDecoration(new SpaceDecoration(controlSpacing));
        backupPhraseList.setLayoutManager(chipsLayoutManager);
        final BackupPhraseAdapter adapter = new BackupPhraseAdapter(new ArrayList<>());
        backupPhraseList.setAdapter(adapter);
    }

    private void addBackupPhrase() {
        this.backupPhraseSubscription = BaseApplication.get()
                .getTokenManager()
                .getWallet()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleBackupPhraseCallback);
    }

    private void handleBackupPhraseCallback(final HDWallet wallet) {
        final String[] backupPhraseList = wallet.getMasterSeed().split(" ");
        final BackupPhraseAdapter adapter = (BackupPhraseAdapter) activity.getBinding().backupPhraseList.getAdapter();
        adapter.setBackupPhraseItems(Arrays.asList(backupPhraseList));
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(this::handleCloseButtonClicked);
        this.activity.getBinding().verifyPhraseBtn.setOnClickListener(this::handleVerifyPhraseButtonClicked);
    }

    private void handleCloseButtonClicked(final View v) {
        this.activity.finish();
    }

    private void handleVerifyPhraseButtonClicked(final View v) {
        final Intent intent = new Intent(this.activity, BackupPhraseVerifyActivity.class);
        this.activity.startActivity(intent);
    }

    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        if (this.backupPhraseSubscription != null) {
            this.backupPhraseSubscription.unsubscribe();
        }
        this.activity = null;
    }
}
