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

import android.content.ClipData;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.flexbox.FlexboxLayout;
import com.tokenbrowser.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DragAndDropView extends LinearLayout {

    private ArrayList<String> correctBackupPhrase;
    private ArrayList<String> userInputtedBackupPhrase;
    private ArrayList<String> remainingInputBackupPhrase;
    private ShadowTextView draggedView;
    private OnFinishedListener onFinishedListener;

    private static final String BUNDLE__CORRECT_BACKUP_PHRASE = "correctBackupPhrase";
    private static final String BUNDLE__USER_INPUTTED_BACKUP_PHRASE = "userInputtedBackupPhrase";
    private static final String BUNDLE__REMAINING_INPUT_BACKUP_PHRASE = "remainingInputBackupPhrase";
    private static final String BUNDLE__SUPER_STATE = "superState";

    public interface OnFinishedListener {
        void onFinish();
    }

    public void setOnFinishedListener(final OnFinishedListener listener) {
        this.onFinishedListener = listener;
    }

    public DragAndDropView(Context context) {
        super(context);
        init();
    }

    public DragAndDropView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public DragAndDropView(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_drag_and_drop, this);
    }

    public void setBackupPhrase(final List<String> backupPhrase) {
        this.correctBackupPhrase = new ArrayList<>(backupPhrase);
        this.userInputtedBackupPhrase = createEmptyArray(backupPhrase.size());
        this.remainingInputBackupPhrase = new ArrayList<>(backupPhrase);
        Collections.shuffle(this.remainingInputBackupPhrase);

        initView();
    }

    private void initView() {
        initChildViews();
        initListeners();
        renderPhraseSegments();
    }

    private void initChildViews() {
        final FlexboxLayout sourceLayout = (FlexboxLayout) findViewById(R.id.remaining_phrases);
        final FlexboxLayout targetLayout = (FlexboxLayout) findViewById(R.id.user_inputted_phrases);
        final FlexboxLayout.LayoutParams phraseParams = generateLayoutParams();

        sourceLayout.removeAllViews();
        targetLayout.removeAllViews();

        for (final String phrase : this.remainingInputBackupPhrase) {
            final ShadowTextView userInputtedTextView = generateTargetTextView();
            final ShadowTextView remainingTextView = generateSourceTextView(phrase);
            sourceLayout.addView(remainingTextView, phraseParams);
            targetLayout.addView(userInputtedTextView, phraseParams);
        }
        sourceLayout.requestLayout();
        targetLayout.requestLayout();
    }

    @NonNull
    private FlexboxLayout.LayoutParams generateLayoutParams() {
        final FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int margin = getResources().getDimensionPixelOffset(R.dimen.backup_phrase_spacing);
        params.setMargins(0, 0, margin, margin);
        return params;
    }

    @NonNull
    private ShadowTextView generateTargetTextView() {
        final ShadowTextView textView = new ShadowTextView(getContext());
        textView
                .setShadowEnabled(false)
                .setCornerRadius(getResources().getDimensionPixelSize(R.dimen.backup_phrase_corner_radius))
                .setBackground(null);
        return textView;
    }

    @NonNull
    private ShadowTextView generateSourceTextView(final String phrase) {
        final ShadowTextView textView = new ShadowTextView(getContext());
        textView
                .setShadowEnabled(true)
                .setCornerRadius(getResources().getDimensionPixelSize(R.dimen.backup_phrase_corner_radius))
                .setText(phrase);
        return textView;
    }

    private void initListeners() {
        final FlexboxLayout sourceLayout = (FlexboxLayout) findViewById(R.id.remaining_phrases);
        for (int i = 0; i < sourceLayout.getChildCount(); i++) {
            final ShadowTextView remainingPhraseView = (ShadowTextView) sourceLayout.getChildAt(i);
            remainingPhraseView.setListener(this.clickAndDragListener);
            remainingPhraseView.setOnDragListener(this::handleDragEvent);
        }

        final FlexboxLayout targetLayout = (FlexboxLayout) findViewById(R.id.user_inputted_phrases);
        for (int i = 0; i < targetLayout.getChildCount(); i++) {
            final ShadowTextView inputtedPhraseView = (ShadowTextView) targetLayout.getChildAt(i);
            inputtedPhraseView.setListener(this.clickAndDragListener);
            inputtedPhraseView.setOnDragListener(this::handleDragEvent);
        }
    }

    private final ShadowTextView.ClickAndDragListener clickAndDragListener = new ShadowTextView.ClickAndDragListener() {
        @Override
        public void onClick(final ShadowTextView v) {
            handlePhraseClicked(v);
        }

        @Override
        public void onDrag(final ShadowTextView v) {
            handlePhraseDragged(v);
        }
    };

    private void handlePhraseClicked(final View v) {
        final ShadowTextView clickedView = (ShadowTextView) v;
        final String clickedPhrase = clickedView.getText();
        if (clickedPhrase.length() == 0) {
            return;
        }

        moveViewToCorrectLocationFromClick(v);
    }

    private boolean handlePhraseDragged(final View v) {
        this.draggedView = (ShadowTextView) v;
        final String clickedPhrase = this.draggedView.getText();
        if (clickedPhrase.length() == 0) {
            return false;
        }

        final ClipData clipData = ClipData.newPlainText("value", clickedPhrase);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.draggedView.startDragAndDrop(clipData, new View.DragShadowBuilder(v), null, 0);
        } else {
            this.draggedView.startDrag(clipData, new View.DragShadowBuilder(v), null, 0);
        }

        return true;
    }

    private boolean handleDragEvent(final View v, final DragEvent event) {
        switch(event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
            case DragEvent.ACTION_DRAG_ENTERED:
            case DragEvent.ACTION_DRAG_LOCATION:
            case DragEvent.ACTION_DRAG_EXITED:
            case DragEvent.ACTION_DRAG_ENDED:
                return true;

            case DragEvent.ACTION_DROP:
                final String phrase = event.getClipData().getItemAt(0).getText().toString();
                moveViewToCorrectLocationFromDrag(v, phrase);
                return true;

            default:
                return false;
        }
    }

    private void moveViewToCorrectLocationFromClick(final View clickedView) {
        final String phrase = ((ShadowTextView)clickedView).getText();
        final FlexboxLayout sourceLayout = (FlexboxLayout) findViewById(R.id.remaining_phrases);
        if (clickedView.getParent().equals(sourceLayout)) {
            addPhraseToUserInputtedPhrases(phrase);
        } else {
            addPhraseToRemainingPhrases(phrase);
        }
    }

    private void moveViewToCorrectLocationFromDrag(final View draggedView, final String phrase) {
        final FlexboxLayout sourceLayout = (FlexboxLayout) findViewById(R.id.remaining_phrases);
        if (draggedView.getParent().equals(sourceLayout)) {
            addPhraseToRemainingPhrases(phrase);
        } else {
            addPhraseToUserInputtedPhrases(phrase);
        }
    }

    private void addPhraseToUserInputtedPhrases(final String phrase) {
        swapPhraseBetweenArrays(phrase, this.remainingInputBackupPhrase, this.userInputtedBackupPhrase);
    }

    private void addPhraseToRemainingPhrases(final String phrase) {
        swapPhraseBetweenArrays(phrase, this.userInputtedBackupPhrase, this.remainingInputBackupPhrase);
    }

    private void swapPhraseBetweenArrays(final String phrase, final ArrayList<String> from, final ArrayList<String> to) {
        for (int i = 0; i < to.size(); i++) {
            final String phraseAtPosition = to.get(i);
            if (phraseAtPosition == null) {
                final int index = from.indexOf(phrase);
                if (index != -1) {
                    from.set(index, null);
                    to.set(i, phrase);
                }
                renderPhraseSegments();
                checkBackupPhrase();
                return;
            }
        }
    }

    private ArrayList<String> createEmptyArray(final int size) {
        final ArrayList<String> retVal = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            retVal.add(null);
        }
        return retVal;
    }

    private void renderPhraseSegments() {
        renderUserInputtedPhrases();
        renderRemainingInputPhrases();
    }

    private void renderUserInputtedPhrases() {
        final FlexboxLayout backupPhraseTargetLayout = (FlexboxLayout) findViewById(R.id.user_inputted_phrases);
        for (int i = 0; i < this.userInputtedBackupPhrase.size(); i++) {
            final ShadowTextView backupPhraseTarget = (ShadowTextView) backupPhraseTargetLayout.getChildAt(i);
            final String inputtedPhrase = this.userInputtedBackupPhrase.get(i);
            setText(backupPhraseTarget, inputtedPhrase);
        }
    }

    private void renderRemainingInputPhrases() {
        final FlexboxLayout gridLayout = (FlexboxLayout) findViewById(R.id.remaining_phrases);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            final ShadowTextView backupPhraseWord = (ShadowTextView) gridLayout.getChildAt(i);
            final String remainingPhrase = this.remainingInputBackupPhrase.get(i);
            setText(backupPhraseWord, remainingPhrase);
        }
    }

    private void setText(final ShadowTextView v, final String text) {
        if (v == null) return;

        v.setText(text);
        final int background = text != null ? R.drawable.background_with_radius : 0;
        v.setBackgroundResource(background);

        v.enableShadow();
        if (text == null) {
            v.disableShadow();
        }
    }

    private void checkBackupPhrase() {
        if (this.correctBackupPhrase.equals(this.userInputtedBackupPhrase)) {
            this.onFinishedListener.onFinish();
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(BUNDLE__CORRECT_BACKUP_PHRASE, this.correctBackupPhrase);
        bundle.putStringArrayList(BUNDLE__USER_INPUTTED_BACKUP_PHRASE, this.userInputtedBackupPhrase);
        bundle.putStringArrayList(BUNDLE__REMAINING_INPUT_BACKUP_PHRASE, this.remainingInputBackupPhrase);
        bundle.putParcelable(BUNDLE__SUPER_STATE, super.onSaveInstanceState());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            final Bundle bundle = (Bundle) state;
            this.correctBackupPhrase = bundle.getStringArrayList(BUNDLE__CORRECT_BACKUP_PHRASE);
            this.userInputtedBackupPhrase = bundle.getStringArrayList(BUNDLE__USER_INPUTTED_BACKUP_PHRASE);
            this.remainingInputBackupPhrase = bundle.getStringArrayList(BUNDLE__REMAINING_INPUT_BACKUP_PHRASE);
            state = bundle.getParcelable(BUNDLE__SUPER_STATE);
        }
        super.onRestoreInstanceState(state);
        initView();
    }
}
