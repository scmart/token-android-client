package com.bakkenbaeck.token.view.adapter;

import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.model.network.App;
import com.bakkenbaeck.token.view.adapter.viewholder.SearchAppHeaderViewHolder;
import com.bakkenbaeck.token.view.adapter.viewholder.SearchAppViewHolder;

import java.util.List;

public class SearchAppAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @IntDef({
            ITEM,
            HEADER,
    })
    public @interface ViewType {}
    private final static int ITEM = 1;
    private final static int HEADER = 2;

    private List<App> apps;

    public SearchAppAdapter(final List<App> apps) {
        this.apps = apps;
    }

    public void addItems(final List<App> apps) {
        this.apps.clear();
        this.apps.addAll(apps);
        this.notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case HEADER: {
                final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_search_header, parent, false);
                return new SearchAppHeaderViewHolder(v);
            }
            case ITEM:
            default: {
                final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item__search_app, parent, false);
                return new SearchAppViewHolder(v);
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final @ViewType int viewType = holder.getItemViewType();

        switch (viewType) {
            case HEADER: {
                break;
            }
            case ITEM:
            default: {
                final SearchAppViewHolder vh = (SearchAppViewHolder) holder;
                final App app = this.apps.get(position - 1);

                vh.setLabel(app);
                vh.setImage(app);
                vh.setCategory(app);
                vh.setRating(3.6);
                break;
            }
        }
    }

    @Override
    public @ViewType int getItemViewType(int position) {
        return position == 0 ? HEADER : ITEM;
    }

    @Override
    public int getItemCount() {
        //Adding one because of the header
        return this.apps.size() + 1;
    }
}
