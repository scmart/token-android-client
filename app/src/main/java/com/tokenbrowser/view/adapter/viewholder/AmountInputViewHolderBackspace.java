package com.tokenbrowser.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tokenbrowser.view.adapter.AmountInputAdapter;

public class AmountInputViewHolderBackspace extends RecyclerView.ViewHolder {

    public AmountInputViewHolderBackspace(View itemView) {
        super(itemView);
    }

    public void bind(final AmountInputAdapter.OnKeyboardItemClicked listener) {
        this.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener == null) {
                    return;
                }

                listener.onBackSpaceClicked();
            }
        });
    }
}
