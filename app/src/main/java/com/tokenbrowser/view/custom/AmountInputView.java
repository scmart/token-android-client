/*
 * 	Copyright (c) 2017. Token Browser, Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tokenbrowser.view.custom;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.tokenbrowser.R;
import com.tokenbrowser.view.adapter.AmountInputAdapter;

public class AmountInputView extends LinearLayout {

    private AmountInputAdapter adapter;

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

    public void setOnAmountClickedListener(final AmountInputAdapter.OnKeyboardItemClicked listener) {
        this.adapter.setOnKeyboardItemClickListener(listener);
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
        this.adapter = new AmountInputAdapter();
        recyclerView.setAdapter(adapter);
    }
}

