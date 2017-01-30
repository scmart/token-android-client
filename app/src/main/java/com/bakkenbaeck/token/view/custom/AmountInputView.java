package com.bakkenbaeck.token.view.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.adapter.AmountInputAdapter;

public class AmountInputView extends LinearLayout {
    public AmountInputView(Context context) {
        super(context);
        init();
    }

    public AmountInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AmountInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_amount_input, this);
        initView();
    }

    private void initView() {
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.keyboard_view);
        recyclerView.setLayoutManager(new NoScrollGridLayoutManager(getContext(), 3));
        final int horizontalSpacing = getContext().getResources().getDimensionPixelOffset(R.dimen.amount_view_horizontal_spacing);
        final int verticalSpacing = getContext().getResources().getDimensionPixelOffset(R.dimen.amount_view_vertical_spacing);
        recyclerView.addItemDecoration(new GridSpacingDecoration(3, horizontalSpacing, verticalSpacing));
        final AmountInputAdapter adapter = new AmountInputAdapter();
        recyclerView.setAdapter(adapter);
    }
}

