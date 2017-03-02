package com.bakkenbaeck.token.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.TextView;

import com.tokenbrowser.token.R;

public class ShadowTextView extends CardView {

    private boolean shadowEnabled;
    private boolean visibleBackground;
    private String text;
    private int cornerRadius;

    public ShadowTextView(@NonNull Context context) {
        super(context);
        init();
    }

    public ShadowTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        parseAttributeSet(context, attrs);
        init();
    }

    private void parseAttributeSet(final Context context, final @Nullable AttributeSet attrs) {
        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ShadowTextView, 0, 0);
        this.shadowEnabled = a.getBoolean(R.styleable.ShadowTextView_shadow, true);
        this.visibleBackground = a.getBoolean(R.styleable.ShadowTextView_visibleBackground, true);
        this.text = a.getString(R.styleable.ShadowTextView_text);
        this.cornerRadius = a.getDimensionPixelSize(R.styleable.ShadowTextView_cornerRadius, 0);
        a.recycle();
    }

    private void init() {
        inflate(getContext(), R.layout.view_shadow_text_view, this);
        initView();
    }

    private void initView() {
        setText(this.text);
        setMaxCardElevation(this.cornerRadius);
        setRadius(this.cornerRadius);

        if (!this.visibleBackground) {
            setBackground(null);
        }
        if (!this.shadowEnabled) {
            disableShadow();
        }
    }

    public void enableShadow() {
        this.setCardElevation(4f);
    }

    public void disableShadow() {
        this.setCardElevation(0f);
    }

    public void setText(final String s) {
        final TextView textView = (TextView) findViewById(R.id.text_view);
        textView.setText(s);
    }

    public String getText() {
        final TextView textView = (TextView) findViewById(R.id.text_view);
        return textView.getText().toString();
    }
}
