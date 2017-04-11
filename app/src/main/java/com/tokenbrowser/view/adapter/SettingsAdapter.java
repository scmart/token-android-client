package com.tokenbrowser.view.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tokenbrowser.R;
import com.tokenbrowser.view.BaseApplication;
import com.tokenbrowser.view.adapter.listeners.OnItemClickListener;
import com.tokenbrowser.view.adapter.viewholder.ClickableViewHolder;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    public static final int LOCAL_CURRENCY = 0;
    public static final int ABOUT = 1;
    public static final int TRANSACTIONS = 2;
    public static final int SIGN_OUT = 3;

    private final String[] settings;
    private OnItemClickListener<Integer> listener;

    public SettingsAdapter() {
        this.settings = BaseApplication.get().getResources().getStringArray(R.array.settings_options);
    }

    public void setOnItemClickListener(final OnItemClickListener<Integer> listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__settings, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final String label = this.settings[position];
        holder.label.setText(label);
        holder.bind(position, listener);
    }

    @Override
    public int getItemCount() {
        return this.settings.length;
    }

    static class ViewHolder extends ClickableViewHolder {
        private TextView label;

        private ViewHolder(final View view) {
            super(view);
            this.label = (TextView) view.findViewById(R.id.label);
        }

        public void bind(final int position, final OnItemClickListener<Integer> listener) {
            this.itemView.setOnClickListener(view -> {
                if (listener == null) {
                    return;
                }

                listener.onItemClick(position);
            });
        }
    }
}
