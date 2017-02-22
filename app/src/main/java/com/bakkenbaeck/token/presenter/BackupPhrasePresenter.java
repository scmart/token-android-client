package com.bakkenbaeck.token.presenter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

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
    private String backupPhrase;

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
        this.backupPhrase = wallet.getMasterSeed();
        final String[] backupPhraseList = this.backupPhrase.split(" ");
        final BackupPhraseAdapter adapter = (BackupPhraseAdapter) activity.getBinding().backupPhraseList.getAdapter();
        adapter.setBackupPhraseItems(Arrays.asList(backupPhraseList));
    }

    private void initClickListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(this::handleCloseButtonClicked);
        this.activity.getBinding().verifyPhraseBtn.setOnClickListener(this::handleVerifyPhraseButtonClicked);
        this.activity.getBinding().copyToClipboard.setOnClickListener(this::handleCopyToClipboardClicked);
    }

    private void handleCloseButtonClicked(final View v) {
        this.activity.finish();
    }

    private void handleVerifyPhraseButtonClicked(final View v) {
        final Intent intent = new Intent(this.activity, BackupPhraseVerifyActivity.class);
        this.activity.startActivity(intent);
    }

    private void handleCopyToClipboardClicked(final View v) {
        final ClipboardManager clipboard = (ClipboardManager) this.activity.getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip = ClipData.newPlainText(this.activity.getString(R.string.backup_phrase), this.backupPhrase);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this.activity, this.activity.getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show();
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
