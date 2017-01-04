package com.bakkenbaeck.token.view.custom;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.bakkenbaeck.token.R;

public class RoundedProgressBar extends View {

    private int width;
    private int height;
    private Paint paint;
    private int backgroundColor = 0xFFFF9800;
    private int foregroundColor = 0xFFFF5722;
    private int progressPercentage;

    private RectF bgLeftArc;
    private RectF fgLeftArc;
    private RectF bgRightArc;
    private RectF fgRightArc;
    private RectF bgBody;
    private RectF fgBody;

    public RoundedProgressBar(final Context context) {
        super(context);
        init();
    }

    public RoundedProgressBar(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        parseAttributeSet(context, attrs);
        init();
    }

    private void parseAttributeSet(final Context context, final @Nullable AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RoundedProgressBar, 0, 0);
        this.backgroundColor = a.getColor(R.styleable.RoundedProgressBar_backgroundColor, 0xFFFF9800);
        this.foregroundColor = a.getColor(R.styleable.RoundedProgressBar_foregroundColor, 0xFFFF5722);
        this.progressPercentage = a.getInteger(R.styleable.RoundedProgressBar_progressPercentage, 0);
        a.recycle();
    }

    private void init() {
        this.paint = new Paint();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int desiredWidth = 300;
        final int desiredHeight = 30;

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.EXACTLY) {
            this.width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            this.width = Math.min(desiredWidth, widthSize);
        } else {
            this.width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            this.height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            this.height = Math.min(desiredHeight, heightSize);
        } else {
            this.height = desiredHeight;
        }

        setMeasuredDimension(this.width, this.height);
    }

    @Override
    protected void onSizeChanged(final int w,
                                 final int h,
                                 final int oldW,
                                 final int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        this.width = w;
        this.height = h;
        generateDrawRects();
    }

    private void generateDrawRects() {
        this.bgLeftArc = new RectF(0, 0, this.height, this.height);
        this.bgRightArc = new RectF(this.width - this.height, 0, this.width, this.height);
        this.bgBody = new RectF(this.height / 2, 0, this.width - (this.height / 2), this.height);

        final int percentageOffset = (int) ((((float)this.width - this.height) / 100) * progressPercentage);

        this.fgLeftArc = new RectF(0, 0, this.height, this.height);
        this.fgRightArc = new RectF(percentageOffset, 0, percentageOffset + this.height, this.height);

        this.fgBody = new RectF(this.height / 2, 0, percentageOffset + (this.height / 2), this.height);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        this.paint.setColor(this.backgroundColor);

        canvas.drawArc(bgLeftArc, 90, 180, true, this.paint);
        canvas.drawArc(bgRightArc, 270, 180, true, this.paint);
        canvas.drawRect(bgBody, this.paint);

        this.paint.setColor(this.foregroundColor);

        canvas.drawArc(fgLeftArc, 90, 180, true, this.paint);
        canvas.drawArc(fgRightArc, 270, 180, true, this.paint);
        canvas.drawRect(fgBody, this.paint);
    }
}
