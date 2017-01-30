package com.bakkenbaeck.token.view.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.bakkenbaeck.token.R;
import com.bakkenbaeck.token.view.adapter.AmountInputAdapter;

public class AmountInputView extends LinearLayout {

    private OnAmountClickedListener listener;

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

    public interface OnAmountClickedListener {
        void handleAmountClicked(final int value);
        void handleBackspaceClicked();
        void handleDotClicked();
    }

    public void setOnAmountClickedListener(final OnAmountClickedListener listener) {
        this.listener = listener;
    }

    private void init() {
        inflate(getContext(), R.layout.view_amount_input, this);
        initView();
    }

    private void initView() {
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new NoScrollGridLayoutManager(getContext(), 3));
        final int horizontalSpacing = getContext().getResources().getDimensionPixelOffset(R.dimen.amount_view_horizontal_spacing);
        final int verticalSpacing = getContext().getResources().getDimensionPixelOffset(R.dimen.amount_view_vertical_spacing);
        recyclerView.addItemDecoration(new GridSpacingDecoration(3, horizontalSpacing, verticalSpacing));
        final AmountInputAdapter adapter = new AmountInputAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setOnKeyboardItemClickListener(this.amountClickedListener);
    }

    private AmountInputAdapter.OnKeyboardItemClicked amountClickedListener = new AmountInputAdapter.OnKeyboardItemClicked() {
        @Override
        public void onNumberClicked(String value) {
            if (listener == null) {
                return;
            }

            final int valueInt = Integer.valueOf(value);
            listener.handleAmountClicked(valueInt);
        }

        @Override
        public void onBackSpaceClicked() {
            if (listener == null) {
                return;
            }

            listener.handleBackspaceClicked();
        }

        @Override
        public void onDotClicked() {
            if (listener == null) {
                return;
            }

            listener.handleDotClicked();
        }
    };
}

