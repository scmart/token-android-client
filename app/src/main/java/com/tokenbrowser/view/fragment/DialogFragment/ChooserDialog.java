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