package com.bakkenbaeck.token.view.custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundCornersImageView extends ImageView{

    public static float radius = 36.0f;

    public RoundCornersImageView(Context context) {
        super(context);
    }

    public RoundCornersImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundCornersImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Path clipPath = new Path();
        clipPath.addRoundRect(new RectF(0, 0, this.getWidth(), this.getHeight()), radius, radius, Path.Direction.CW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }
}
