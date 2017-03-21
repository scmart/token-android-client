package com.tokenbrowser.presenter;

import android.content.Intent;
import android.widget.Toast;

import com.tokenbrowser.R;
import com.tokenbrowser.crypto.HDWallet;
import com.tokenbrowser.util.SharedPrefsUtil;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.activity.MainActivity;
import com.tokenbrowser.view.activity.SignInActivity;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class SignInPresenter implements Presenter<SignInActivity> {

    private SignInActivity activity;
    private boolean firstTimeAttaching = true;
    private CompositeSubscription subscriptions;

    @Override
    public void onViewAttached(SignInActivity view) {
        this.activity = view;
        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
        }
        initClickListeners();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
    }

    private void initClickListeners() {
        this.activity.getBinding().signIn.setOnClickListener(v -> handleSignInClicked());
    }

    private void handleSignInClicked() {
        final String passphraseInput = this.activity.getBinding().password.getText().toString();
        final String[] passphraseArray = passphraseInput.split(" ");
        if (passphraseArray.length != 12) {
            Toast.makeText(
                    this.activity,
                    this.activity.getString(R.string.sign_in_length_error_message),
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        tryCreateWallet(passphraseInput);
    }

    private void tryCreateWallet(final String masterSeed) {
        final Subscription sub = new HDWallet().createFromMasterSeed(masterSeed)
                .flatMap(wallet -> BaseApplication
                        .get()
                        .getTokenManager()
                        .init(wallet))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        unused -> goToMainActivity(),
                        throwable -> handleError());

        this.subscriptions.add(sub);
    }

    private void handleError() {
        Toast.makeText(
                this.activity,
                this.activity.getString(R.string.unable_to_restore_wallet),
                Toast.LENGTH_SHORT)
                .show();
    }

    private void goToMainActivity() {
        SharedPrefsUtil.setSignedIn();
        final Intent intent = new Intent(this.activity, MainActivity.class);
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
        this.subscriptions = null;
        this.activity = null;
    }
}
