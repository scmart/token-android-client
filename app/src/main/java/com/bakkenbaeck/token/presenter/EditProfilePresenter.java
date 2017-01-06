package com.bakkenbaeck.token.presenter;


import com.bakkenbaeck.token.view.fragment.children.EditProfileFragment;

public class EditProfilePresenter implements Presenter<EditProfileFragment> {

    private EditProfileFragment activity;
    private boolean firstTimeAttached = true;

    @Override
    public void onViewAttached(final EditProfileFragment activity) {
        this.activity = activity;
        if (this.firstTimeAttached) {
            this.firstTimeAttached = false;
        }

        initShortLivingObjects();
    }

    private void initShortLivingObjects() {
        attachButtonListeners();
    }

    private void attachButtonListeners() {
        //this.activity.getBinding().backButton.setOnClickListener(this.backButtonClicked);
    }
/*
    private final OnSingleClickListener backButtonClicked = new OnSingleClickListener() {
        @Override
        public void onSingleClick(final View v) {
            activity.onBackPressed();
            activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
    };
*/
    @Override
    public void onViewDetached() {
        this.activity = null;
    }

    @Override
    public void onViewDestroyed() {
        this.activity = null;
    }
}
