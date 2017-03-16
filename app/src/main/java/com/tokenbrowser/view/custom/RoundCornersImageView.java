package com.tokenbrowser.view.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.tokenbrowser.token.R;

import java.io.File;

public class RoundCornersImageView extends FrameLayout {

    public RoundCornersImageView(Context context) {
        super(context);
        init();
    }

    public RoundCornersImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RoundCornersImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        inflate(getContext(), R.layout.view_round_corners, this);
    }

    public void setImage(final File file) {
        final ImageView imageView = (ImageView) findViewById(R.id.image);
        final ImageView frame = (ImageView) findViewById(R.id.frame);

        final GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(imageView);
        Glide.with(getContext())
                .load(file)
                .crossFade()
                .listener(new RequestListener<File, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, File model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(final GlideDrawable resource,
                                                   final File model,
                                                   final Target<GlideDrawable> target,
                                                   final boolean isFromMemoryCache,
                                                   final boolean isFirstResource) {
                        final float heightRatio = ((GlideDrawableImageViewTarget) target).getView().getHeight() / (float) resource.getIntrinsicHeight();
                        final float widthRatio = ((GlideDrawableImageViewTarget) target).getView().getWidth() / (float) resource.getIntrinsicWidth();
                        final int padding = getResources().getDimensionPixelSize(R.dimen.image_frame_padding);

                        if (heightRatio < widthRatio) {
                            final int height = ((GlideDrawableImageViewTarget) target).getView().getHeight() + padding;
                            final int width = (int) (resource.getIntrinsicWidth() * heightRatio) + padding;
                            frame.setLayoutParams(new FrameLayout.LayoutParams(width, height, Gravity.CENTER));
                        } else {
                            final int height = (int) (resource.getIntrinsicHeight() * widthRatio) + padding;
                            final int width = ((GlideDrawableImageViewTarget) target).getView().getWidth() + padding;
                            frame.setLayoutParams(new FrameLayout.LayoutParams(width, height, Gravity.CENTER));
                        }

                        return false;
                    }
                })
                .into(imageViewTarget);
    }
}