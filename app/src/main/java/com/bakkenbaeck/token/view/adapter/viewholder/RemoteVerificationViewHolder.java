package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.adapter.MessageAdapter;

public class RemoteVerificationViewHolder extends RecyclerView.ViewHolder{
    public TextView message;
    public TextView verificationButton;

    public RemoteVerificationViewHolder(View v) {
        super(v);

        message = (TextView) v.findViewById(R.id.verification_message);
        verificationButton = (TextView) v.findViewById(R.id.verifyBtn);
    }

    public void bind(final MessageAdapter.OnVerifyClicklistener listener) {
        verificationButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if(listener != null) {
                    listener.onVerifyClicked();
                }
            }
        });
    }
}
