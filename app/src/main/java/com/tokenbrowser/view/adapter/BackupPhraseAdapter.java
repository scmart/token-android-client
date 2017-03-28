package com.tokenbrowser.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tokenbrowser.R;
import com.tokenbrowser.view.adapter.viewholder.BackupPhraseViewHolder;

import java.util.List;

public class BackupPhraseAdapter extends RecyclerView.Adapter<BackupPhraseViewHolder> {

    private List<String> backupPhrase;

    public BackupPhraseAdapter(final List<String> backupPhrase) {
        this.backupPhrase = backupPhrase;
    }

    public void setBackupPhraseItems(final List<String> backupPhrase) {
        this.backupPhrase.clear();
        this.backupPhrase.addAll(backupPhrase);
        this.notifyDataSetChanged();
    }

    @Override
    public BackupPhraseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__backup_phrase, parent, false);
        return new BackupPhraseViewHolder(v);
    }

    @Override
    public void onBindViewHolder(BackupPhraseViewHolder holder, int position) {
        final String word = this.backupPhrase.get(position);
        holder.setText(word);
    }

    @Override
    public int getItemCount() {
        return this.backupPhrase.size();
    }
}
