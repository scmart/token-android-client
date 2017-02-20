package com.bakkenbaeck.token.view.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bakkenbaeck.token.R;

public class BackupPhraseViewHolder extends RecyclerView.ViewHolder {

    private TextView word;

    public BackupPhraseViewHolder(View itemView) {
        super(itemView);

        this.word = (TextView) itemView.findViewById(R.id.word);
    }

    public void setText(final String word) {
        this.word.setText(word);
    }
}
