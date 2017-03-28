package com.tokenbrowser.view.custom;

import android.content.ClipData;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.tokenbrowser.R;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DragAndDropView extends LinearLayout {

    private List<String> correctBackupPhrase;
    private List<String> userInputtedBackupPhrase;
    private List<String> remainingInputBackupPhrase;
    private ShadowTextView draggedView;
    private OnFinishedListener listener;

    public interface OnFinishedListener {
        void onFinish();
    }

    public void setOnFinishedListener(final OnFinishedListener listener) {
        this.listener = listener;
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
        initListeners();
    }

    private void initListeners() {
        final FlowLayout sourceLayout = (FlowLayout) findViewById(R.id.remaining_phrases);
        for (int i = 0; i < sourceLayout.getChildCount(); i++) {
            final ShadowTextView backupPhraseSource = (ShadowTextView) sourceLayout.getChildAt(i);
            backupPhraseSource.setOnClickListener(this::handleClickEventSource);
            backupPhraseSource.setOnLongClickListener(this::handleLongClickEvent);
            backupPhraseSource.setOnDragListener(this::handleDragEvent);
        }

        final FlowLayout targetLayout = (FlowLayout) findViewById(R.id.user_inputted_phrases);
        for (int i = 0; i < targetLayout.getChildCount(); i++) {
            final ShadowTextView backupPhraseTarget = (ShadowTextView) targetLayout.getChildAt(i);
            backupPhraseTarget.setOnClickListener(this::handleClickEventTarget);
            backupPhraseTarget.setOnLongClickListener(this::handleLongClickEvent);
            backupPhraseTarget.setOnDragListener(this::handleDragEvent);
        }
    }

    private void handleClickEventSource(final View v) {
        final ShadowTextView clickedView = (ShadowTextView) v;
        final String clickedPhrase = clickedView.getText();
        if (clickedPhrase.length() == 0) {
            return;
        }

        addPhraseToUserInputtedPhrases(clickedPhrase);
    }

    private void handleClickEventTarget(final View v) {
        final ShadowTextView clickedView = (ShadowTextView) v;
        final String clickedPhrase = clickedView.getText();
        if (clickedPhrase.length() == 0) {
            return;
        }

        addPhraseToRemainingPhrases(clickedPhrase);
    }

    private boolean handleLongClickEvent(final View v) {
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
                final String droppedPhrase = event.getClipData().getItemAt(0).getText().toString();
                addPhraseToUnknownDestination(droppedPhrase);
                return true;

            default:
                return false;
        }
    }

    private void addPhraseToUnknownDestination(final String phrase) {
        if (this.userInputtedBackupPhrase.contains(phrase)) {
            addPhraseToRemainingPhrases(phrase);
        } else {
            addPhraseToUserInputtedPhrases(phrase);
        }
    }

    private void addPhraseToUserInputtedPhrases(final String phrase) {
        for (int i = 0; i < this.userInputtedBackupPhrase.size(); i++) {
            final String phraseAtPosition = this.userInputtedBackupPhrase.get(i);

            if (phraseAtPosition == null) {
                Collections.replaceAll(this.remainingInputBackupPhrase, phrase, null);
                this.userInputtedBackupPhrase.set(i, phrase);
                renderPhraseSegments();
                checkBackupPhrase();
                return;
            }
        }
    }

    private void addPhraseToRemainingPhrases(final String phrase) {
        for (int i = 0; i < this.remainingInputBackupPhrase.size(); i++) {
            final String phraseAtPosition = this.remainingInputBackupPhrase.get(i);
            if (phraseAtPosition == null) {
                Collections.replaceAll(this.userInputtedBackupPhrase, phrase, null);
                this.remainingInputBackupPhrase.set(i, phrase);
                renderPhraseSegments();
                checkBackupPhrase();
                return;
            }
        }
    }

    public void setBackupPhrase(final List<String> backupPhrase) {
        this.correctBackupPhrase = new ArrayList<>(backupPhrase);
        this.userInputtedBackupPhrase = createEmptyArray(backupPhrase.size());
        this.remainingInputBackupPhrase = new ArrayList<>(backupPhrase);
        Collections.shuffle(this.remainingInputBackupPhrase);

        renderPhraseSegments();
    }

    private List<String> createEmptyArray(final int size) {
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
        final FlowLayout backupPhraseTargetLayout = (FlowLayout) findViewById(R.id.user_inputted_phrases);
        for (int i = 0; i < this.userInputtedBackupPhrase.size(); i++) {
            final ShadowTextView backupPhraseTarget = (ShadowTextView) backupPhraseTargetLayout.getChildAt(i);
            final String inputtedPhrase = this.userInputtedBackupPhrase.get(i);
            setText(backupPhraseTarget, inputtedPhrase);
        }
    }

    private void renderRemainingInputPhrases() {
        final FlowLayout gridLayout = (FlowLayout) findViewById(R.id.remaining_phrases);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            final ShadowTextView backupPhraseWord = (ShadowTextView) gridLayout.getChildAt(i);
            final String remainingPhrase = this.remainingInputBackupPhrase.get(i);
            setText(backupPhraseWord, remainingPhrase);
        }
    }

    private void setText(final ShadowTextView v, final String text) {
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
            this.listener.onFinish();
        }
    }
}
