package com.bakkenbaeck.token.presenter;

import android.view.View;

import com.bakkenbaeck.token.view.activity.AmountActivity;
import com.bakkenbaeck.token.view.custom.AmountInputView;

public class AmountPresenter implements Presenter<AmountActivity> {

    private AmountActivity activity;

    @Override
    public void onViewAttached(AmountActivity view) {
        this.activity = view;
        initView();
    }

    private void initView() {
        this.activity.getBinding().usdValue.setText(String.valueOf("0"));
        this.activity.getBinding().ethValue.setText(String.valueOf("0"));
        this.activity.getBinding().amountInputView.setOnAmountClickedListener(this.amountClickedListener);
        this.activity.getBinding().btnContinue.setOnClickListener(this.continueClickListener);
    }

    private View.OnClickListener continueClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //Show contacts view
        }
    };

    private AmountInputView.OnAmountClickedListener amountClickedListener = new AmountInputView.OnAmountClickedListener() {
        @Override
        public void handleAmountClicked(int value) {
            setValue(value);
        }

        @Override
        public void handleBackspaceClicked() {
            backspace();
        }

        @Override
        public void handleDotClicked() {
            dot();
        }
    };

    private void dot() {
        final String currentStateUsd = this.activity.getBinding().usdValue.getText().toString();
        final String currentStateEth = this.activity.getBinding().ethValue.getText().toString();

        if (currentStateUsd.contains(".")) {
            return;
        }

        final String newStateUsd = currentStateUsd + ".";
        final String newStateEth = currentStateEth + ".";

        this.activity.getBinding().usdValue.setText(newStateUsd);
        this.activity.getBinding().ethValue.setText(newStateEth);
    }

    private void backspace() {
        final String currentStateUsd = this.activity.getBinding().usdValue.getText().toString();
        final String currentStateEth = this.activity.getBinding().ethValue.getText().toString();

        if (currentStateUsd.length() == 0) {
            return;
        }

        final boolean biggerThanOne = currentStateUsd.length() <= 1;
        final boolean isFirstZero = currentStateUsd.substring(0, currentStateUsd.length() - 1).equals("0");

        if (biggerThanOne && isFirstZero) {
            return;
        }

        final String newStateUsd = currentStateUsd.substring(0, currentStateUsd.length() - 1);
        final String newStateEth = currentStateEth.substring(0, currentStateEth.length() - 1);

        this.activity.getBinding().usdValue.setText(newStateUsd);
        this.activity.getBinding().ethValue.setText(newStateEth);
    }

    private void setValue(final int value) {
        final String currentStateUsd = this.activity.getBinding().usdValue.getText().toString();
        final String currentStateEth = this.activity.getBinding().ethValue.getText().toString();

        if (currentStateUsd.length() > 10) {
            return;
        }

        String newStateUsd = currentStateUsd + String.valueOf(value);
        String newStateEth = currentStateEth + String.valueOf(value);

        this.activity.getBinding().usdValue.setText(newStateUsd);
        this.activity.getBinding().ethValue.setText(newStateEth);
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
