package com.bakkenbaeck.token.view.custom;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.journeyapps.barcodescanner.ViewfinderView;

public class FramedViewfinderView extends ViewfinderView {
    private Path path;
    private Paint pathPaint;

    public FramedViewfinderView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    private void initPathIfNeeded() {
        if (this.pathPaint != null) {
            return;
        }

        final Rect frame = super.framingRect;
        final float radius = 25.0f;
        final int strokeWidth = 10;
        final int offset = strokeWidth / 2;

        this.pathPaint = new Paint();
        this.pathPaint.setColor(Color.WHITE);
        this.pathPaint.setStrokeWidth(strokeWidth);
        this.pathPaint.setStyle(Paint.Style.STROKE);

        this.path = new Path();
        this.path.moveTo(frame.left + radius, frame.top - offset);
        this.path.lineTo(frame.left + frame.width() + offset, frame.top - offset);
        this.path.lineTo(frame.left + frame.width() + offset, frame.top + frame.height() + offset);
        this.path.lineTo(frame.left - offset, frame.top + frame.height() + offset);
        this.path.lineTo(frame.left - offset, frame.top - offset);
        this.path.lineTo(frame.left + frame.width() - radius, frame.top - offset);

        final CornerPathEffect cornerPathEffect = new CornerPathEffect(radius);
        this.pathPaint.setPathEffect(cornerPathEffect);
    }

    @Override
    public void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        final Rect frame = super.framingRect;
        if (frame == null) {
            return;
        }

        initPathIfNeeded();
        canvas.drawPath(this.path, this.pathPaint);
    }
}
