package com.bakkenbaeck.toshi.view.adapter.viewholder;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.toshi.R;

import jp.wasabeef.recyclerview.animators.holder.AnimateViewHolder;

public final class LocalTextViewHolder extends AnimateViewHolder {
    public TextView messageText;

    public LocalTextViewHolder(final View v) {
        super(v);
        this.messageText = (TextView) v.findViewById(R.id.single_message_item__message);
    }

    @Override
    public void preAnimateAddImpl() {
        ViewCompat.setTranslationX(itemView, itemView.getWidth() * 0.3f);
        ViewCompat.setAlpha(itemView, 0);
    }

    @Override
    public void animateAddImpl(final ViewPropertyAnimatorListener listener) {
        ViewCompat.animate(itemView)
                .translationX(0)
                .alpha(1)
                .setDuration(150)
                .setListener(listener)
                .start();
    }

    @Override
    public void animateRemoveImpl(final ViewPropertyAnimatorListener listener) {

    }
}
