package com.bakkenbaeck.toshi.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.toshi.R;
import com.bakkenbaeck.toshi.view.adapter.viewholder.WalletAddressViewHolder;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.subjects.PublishSubject;

public final class WalletAddressesAdapter extends RecyclerView.Adapter<WalletAddressViewHolder> {
    private List<String> addresses;
    private final PublishSubject<String> onClickSubject = PublishSubject.create();

    public WalletAddressesAdapter() {
        this.addresses = new ArrayList<> ();
        this.addresses.add("0xc2f651fD599627a33962aA7df020bf004De9A958");
        this.addresses.add("0x737B8f37C518453B98A9Ef82Ac38E67143DBccfF");
        this.addresses.add("0xa5453837CEF269664dcf70f76992D7F5C3D9e3b9");
        this.addresses.add("0xfc67DC70845aA54cb175d86C7c95090697De01f2");
        this.addresses.add("0x8e799B043191393A09089f429D2BC6DB97c2B02a");
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
