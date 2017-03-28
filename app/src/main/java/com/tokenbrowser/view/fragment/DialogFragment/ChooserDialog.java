package com.tokenbrowser.view.fragment.DialogFragment;

import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.tokenbrowser.R;
import com.tokenbrowser.databinding.FragmentChooserBinding;

public class ChooserDialog extends DialogFragment {

    public static final String TAG = "ChooserDialog";
    private FragmentChooserBinding binding;
    private OnChooserClickListener listener;

    public static ChooserDialog newInstance() {
        return new ChooserDialog();
    }

    public interface OnChooserClickListener {
        void captureImageClicked();
        void importImageFromGalleryClicked();
    }

    public void setOnChooserClickListener(final OnChooserClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@NonNull Bundle state) {
        final Dialog dialog = super.onCreateDialog(state);
        if (dialog.getWindow() != null) {
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chooser, container, false);
        initClickListeners();
        return this.binding.getRoot();
    }

    private void initClickListeners() {
        this.binding.captureImage.setOnClickListener(this::handleCaptureImageClicked);
        this.binding.galleryImage.setOnClickListener(this::handleImportGalleryImageClicked);
    }

    private void handleCaptureImageClicked(final View v) {
        if (this.listener == null) return;
        this.listener.captureImageClicked();
        this.dismiss();
    }

    private void handleImportGalleryImageClicked(final View v) {
        if (this.listener == null) return;
        this.listener.importImageFromGalleryClicked();
        this.dismiss();
    }
}