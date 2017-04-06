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
import rx.subscriptions.CompositeSubscription;

public class SignInPresenter implements Presenter<SignInActivity> {

    private SignInActivity activity;
    private boolean firstTimeAttaching = true;
    private CompositeSubscription subscriptions;
    private boolean onGoingTask = false;

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
        this.activity.getBinding().createNewAccount.setOnClickListener(v -> handleCreateNewAccountClicked());
    }

    private void handleSignInClicked() {
        final String passphraseInput = this.activity.getBinding().password.getText().toString().toLowerCase().trim();
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
        if (this.onGoingTask) return;
        this.onGoingTask = true;

        final Subscription sub = new HDWallet().createFromMasterSeed(masterSeed)
                .flatMap(wallet -> BaseApplication
                        .get()
                        .getTokenManager()
                        .init(wallet))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        unused -> handleSuccess(),
                        throwable -> handleError());

        this.subscriptions.add(sub);
    }

    private void handleSuccess() {
        this.onGoingTask = false;
        goToMainActivity();
    }

    private void handleError() {
        this.onGoingTask = false;
        Toast.makeText(
                this.activity,
                this.activity.getString(R.string.unable_to_restore_wallet),
                Toast.LENGTH_SHORT)
                .show();
    }

    private void goToMainActivity() {
        SharedPrefsUtil.setSignedIn();
        // We just assume they have backed up their phrase since they just managed to log in with a phrase
        SharedPrefsUtil.setHasBackedUpPhrase();
        final Intent intent = new Intent(this.activity, MainActivity.class);
        this.activity.startActivity(intent);
        this.activity.finish();
    }

    private void handleCreateNewAccountClicked() {
        if (this.onGoingTask) return;
        this.onGoingTask = true;

        final Subscription sub =
                BaseApplication
                .get()
                .getTokenManager()
                .init()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(unused -> handleSuccess());

        this.subscriptions.add(sub);
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
