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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.tokenbrowser.R;

public class RatingBar extends View {

    private int width;
    private int height;
    private int backgroundColor;
    private int foregroundColor;
    private int percentage;
    private int radius;

    private Paint paint;
    private RectF bgBody;
    private RectF fgBody;

    public RatingBar(final Context context) {
        super(context);
        init();
    }

    public RatingBar(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        parseAttributeSet(context, attrs);
        init();
    }

    private void parseAttributeSet(final Context context, final @Nullable AttributeSet attrs) {
        final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RatingBar, 0, 0);
        this.backgroundColor = a.getColor(R.styleable.RatingBar_backgroundColor, 0);
        this.foregroundColor = a.getColor(R.styleable.RatingBar_foregroundColor, 0);
        this.percentage = a.getInteger(R.styleable.RatingBar_progressPercentage, 0);
        this.radius = a.getDimensionPixelSize(R.styleable.RatingBar_radius, 0);
        a.recycle();
    }

    private void init() {
        this.paint = new Paint();
    }

    public void setPercentage(final int percentage) {
        final int minPercentage = 5;
        this.percentage = Math.min(percentage, 100);
        this.percentage = Math.max(this.percentage, minPercentage);
        generateDrawRects();
        this.invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, final int heightMeasureSpec) {
        final int desiredWidth = this.getResources().getDimensionPixelSize(R.dimen.rating_bar_width);
        final int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);

        if (desiredWidth > 0 && desiredWidth < measuredWidth) {
            final int measureMode = MeasureSpec.getMode(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(desiredWidth, measureMode);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
        this.bgBody = new RectF(0, 0, this.width, this.height);
        final int percentageOffset = (int) (((float)this.width / 100) * this.percentage);
        this.fgBody = new RectF(0, 0, percentageOffset, this.height);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        this.paint.setColor(this.backgroundColor);
        canvas.drawRoundRect(this.bgBody, this.radius, this.radius, this.paint);
        this.paint.setColor(this.foregroundColor);
        canvas.drawRoundRect(this.fgBody, this.radius, this.radius, this.paint);
    }
}
