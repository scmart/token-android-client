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
