package com.bakkenbaeck.token.view.fragment.toplevel;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.presenter.PlaceholderPresenter;
import com.bakkenbaeck.token.presenter.factory.PlaceholderPresenterFactory;
import com.bakkenbaeck.token.presenter.factory.PresenterFactory;
import com.bakkenbaeck.token.view.fragment.BasePresenterFragment;

public class PlaceholderFragment extends BasePresenterFragment<PlaceholderPresenter, PlaceholderFragment> {

    private TextView textView;

    public static PlaceholderFragment newInstance(final CharSequence title) {
        final PlaceholderFragment f = new PlaceholderFragment();
        final Bundle b = new Bundle();
        b.putCharSequence("title", title);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, final @Nullable Bundle inState) {
        final CharSequence title = getArguments().getCharSequence("title", null);
        final View v =  inflater.inflate(R.layout.fragment_placeholder, container, false);
        ((TextView)v.findViewById(R.id.title)).setText(title);

        this.textView = (TextView) v.findViewById(R.id.placeholder_text);
        return v;
    }

    @NonNull
    @Override
    protected PresenterFactory<PlaceholderPresenter> getPresenterFactory() {
        return new PlaceholderPresenterFactory();
    }

    @Override
    protected void onPresenterPrepared(@NonNull final PlaceholderPresenter presenter) {
        // Do nothing
    }

    @Override
    protected int loaderId() {
        return 0;
    }

    public void setText(final String text) {
        if (this.textView != null) {
            this.textView.setText(Html.fromHtml(text));
            this.textView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }
}
