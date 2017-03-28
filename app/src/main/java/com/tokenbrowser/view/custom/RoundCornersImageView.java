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
import com.tokenbrowser.R;

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
        final ImageView image = (ImageView) findViewById(R.id.rciv__image);
        final ImageView frame = (ImageView) findViewById(R.id.rciv__frame);
        
        final GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(image);
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

                        resetSizes();
                        final int targetWidth = getResources().getDimensionPixelSize(R.dimen.round_corners_image_view__default_size);
                        final int targetHeight = getResources().getDimensionPixelSize(R.dimen.round_corners_image_view__default_size);

                        final float widthRatio = targetWidth / (float) resource.getIntrinsicWidth();
                        final float heightRatio = targetHeight / (float) resource.getIntrinsicHeight();

                        final int padding = getResources().getDimensionPixelSize(R.dimen.image_frame_padding);

                        if (heightRatio < widthRatio) {
                            final int width = (int) (resource.getIntrinsicWidth() * heightRatio);
                            final int height = targetHeight;
                            resizeFrameAndImage(width, height, padding);
                        } else {
                            final int width = targetWidth;
                            final int height = (int) (resource.getIntrinsicHeight() * widthRatio);
                            resizeFrameAndImage(width, height, padding);
                        }

                        return false;
                    }

                    private void resetSizes() {
                        final int defaultSize = getResources().getDimensionPixelSize(R.dimen.round_corners_image_view__default_size);
                        final int padding = getResources().getDimensionPixelSize(R.dimen.image_frame_padding) * -1;
                        resizeFrameAndImage(defaultSize, defaultSize, padding);
                    }

                    private void resizeFrameAndImage(final int width, final int height, final int padding) {
                        final int gravity =
                                getLayoutParams() instanceof FrameLayout.LayoutParams
                                        ? ((LayoutParams)getLayoutParams()).gravity
                                        : Gravity.LEFT;

                        frame.setLayoutParams(new LayoutParams(width, height, gravity));

                        final LayoutParams imageParams = new LayoutParams(width - padding, height - padding, gravity);
                        final int leftPadding =
                                gravity == Gravity.LEFT || gravity == -1
                                        ? padding / 2
                                        : 0;
                        final int rightPadding =
                                gravity == Gravity.RIGHT
                                        ? padding / 2
                                        : 0;
                        imageParams.setMargins(leftPadding, padding / 2, rightPadding, 0);

                        image.setLayoutParams(imageParams);
                    }

                })
                .into(imageViewTarget);
    }
}