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

                        if (heightRatio < widthRatio) {
                            final int height = ((GlideDrawableImageViewTarget) target).getView().getHeight();
                            final int width = (int) (resource.getIntrinsicWidth() * heightRatio);
                            resizeFrameAndImage(height, width);
                        } else {
                            final int height = (int) (resource.getIntrinsicHeight() * widthRatio);
                            final int width = ((GlideDrawableImageViewTarget) target).getView().getWidth();
                            resizeFrameAndImage(height, width);
                        }

                        return false;
                    }

                    private void resizeFrameAndImage(final int height, final int width) {
                        final int gravity =
                                getLayoutParams() instanceof FrameLayout.LayoutParams
                                        ? ((LayoutParams)getLayoutParams()).gravity
                                        : Gravity.LEFT;

                        final int padding = getResources().getDimensionPixelSize(R.dimen.image_frame_padding);

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
                        imageView.setLayoutParams(imageParams);
                    }
                })
                .into(imageViewTarget);
    }
}