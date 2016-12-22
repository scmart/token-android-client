package com.bakkenbaeck.token.view.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.databinding.ActivityChatBinding;
import com.bakkenbaeck.token.model.Contact;
import com.bakkenbaeck.token.presenter.ChatPresenter;
import com.bakkenbaeck.token.presenter.factory.ChatPresenterFactory;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;

public final class ChatActivity extends BasePresenterActivity<ChatPresenter, ChatActivity> {
    public static final String EXTRA__CONTACT = "contact";

    private static final int UNIQUE_ACTIVITY_ID = 4002;
    private ActivityChatBinding binding;
    private Contact contact;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        this.binding = DataBindingUtil.setContentView(this, R.layout.activity_chat);
        this.contact = getIntent().getParcelableExtra(EXTRA__CONTACT);
/*
        SpeedyLinearLayoutManager linearLayoutManager = new SpeedyLinearLayoutManager(this);
        this.binding.messagesList.setLayoutManager(linearLayoutManager);

        SlideUpAnimator anim;

        if(Build.VERSION.SDK_INT >= 21){
            anim = new SlideUpAnimator(new PathInterpolator(0.33f, 0.78f, 0.3f, 1));
        }else{
            anim = new SlideUpAnimator(new DecelerateInterpolator());
        }

        anim.setAddDuration(400);
        this.binding.messagesList.setItemAnimator(anim);

        float offsetPx = getResources().getDimension(R.dimen.bottom_offset_dp);
        BottomOffsetDecoration bottomOffsetDecoration = new BottomOffsetDecoration((int) offsetPx);
        getBinding().messagesList.addItemDecoration(bottomOffsetDecoration);*/
    }

    public final ActivityChatBinding getBinding() {
        return this.binding;
    }

    @NonNull
    @Override
    protected PresenterFactory<ChatPresenter> getPresenterFactory() {
        return new ChatPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final ChatPresenter presenter) {
        presenter.setPassedInContact(this.contact);
    }

    @Override
    protected int loaderId() {
        return UNIQUE_ACTIVITY_ID;
    }
}
