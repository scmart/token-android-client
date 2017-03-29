package com.tokenbrowser.view.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.TextView;

import com.tokenbrowser.R;

public class ShadowTextView extends CardView {

    private boolean shadowEnabled;
    private boolean visibleBackground;
    private String text;
    private int cornerRadius;
    private float touchDownX;
    private float touchDownY;
    private boolean isClicked;
    private ClickAndDragListener listener;

    public interface ClickAndDragListener {
        void onClick(ShadowTextView v);
        void onDrag(ShadowTextView v);
    }

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

    public void setListener(final ClickAndDragListener listener) {
        this.listener = listener;
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

    @Override
    public boolean onTouchEvent(final MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                this.touchDownX = ev.getX();
                this.touchDownY = ev.getY();
                this.isClicked = true;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isClicked) {
                    if (this.listener != null) this.listener.onClick(this);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                final float SCROLL_THRESHOLD = 10;
                if (this.isClicked && (Math.abs(touchDownX - ev.getX()) > SCROLL_THRESHOLD || Math.abs(touchDownY - ev.getY()) > SCROLL_THRESHOLD)) {
                    if (this.listener != null) this.listener.onDrag(this);
                    isClicked = false;
                }
                break;
            default:
                break;
        }
        return true;
    }
}
