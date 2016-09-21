package com.bakkenbaeck.toshi.view.adapter;

import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.toshi.R;
import com.bakkenbaeck.toshi.view.BaseApplication;
import com.bakkenbaeck.toshi.view.adapter.viewholder.WalletAddressViewHolder;
import com.securepreferences.SecurePreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class WalletAddressesAdapter extends RecyclerView.Adapter<WalletAddressViewHolder> {

    private static final String WALLET_ADDRESSES = "wa";

    private List<String> addresses;
    private final PublishSubject<String> onClickSubject = PublishSubject.create();
    private final SharedPreferences prefs;

    public WalletAddressesAdapter() {
        this.addresses = new ArrayList<>();
        this.prefs = new SecurePreferences(BaseApplication.get(), "", "waa");
        loadFromPreferences();
        notifyDataSetChanged();
    }

    private void loadFromPreferences() {
        final Set<String> previousAddresses = this.prefs.getStringSet(WALLET_ADDRESSES, null);
        if (previousAddresses != null) {
            this.addresses.addAll(previousAddresses);
        }
    }

    // Will trim duplicates
    public void addAddress(final String address) {
        // Convert addresses to Set for saving
        final Set<String> addressesToSave = new HashSet<>();
        addressesToSave.addAll(this.addresses);
        addressesToSave.add(address);
        this.prefs
                .edit()
                .putStringSet(WALLET_ADDRESSES, addressesToSave)
                .apply();

        // Recast the set to List for rendeing (will clear out dupes)
        this.addresses.clear();
        this.addresses.addAll(addressesToSave);
        notifyDataSetChanged();
    }

    @Override
    public final WalletAddressViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__wallet_address, parent, false);
        return new WalletAddressViewHolder(v);
    }

    @Override
    public final void onBindViewHolder(final WalletAddressViewHolder holder, final int position) {
        final String address = this.addresses.get(position);
        holder.addressText.setText(address);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSubject.onNext(address);
            }
        });
    }

    @Override
    public final int getItemCount() {
        return addresses.size();
    }

    public Observable<String> getPositionClicks(){
        return onClickSubject.asObservable();
    }

}
