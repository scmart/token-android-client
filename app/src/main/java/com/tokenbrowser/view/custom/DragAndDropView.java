package com.tokenbrowser.view.custom;

import android.content.ClipData;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.tokenbrowser.token.R;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DragAndDropView extends LinearLayout {

    private ShadowTextView selectedBackupPhrase;
    private List<String> backupPhrase;
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
        final FlowLayout sourceLayout = (FlowLayout) findViewById(R.id.backup_phrase_source);
        for (int i = 0; i < sourceLayout.getChildCount(); i++) {
            final ShadowTextView backupPhraseSource = (ShadowTextView) sourceLayout.getChildAt(i);
            backupPhraseSource.setOnClickListener(this::handleClickEventSource);
            backupPhraseSource.setOnLongClickListener(this::handleLongClickEvent);
            backupPhraseSource.setOnDragListener(this::handleDragEvent);
        }

        final FlowLayout targetLayout = (FlowLayout) findViewById(R.id.backup_phrase_target);
        for (int i = 0; i < targetLayout.getChildCount(); i++) {
            final ShadowTextView backupPhraseTarget = (ShadowTextView) targetLayout.getChildAt(i);
            backupPhraseTarget.setOnClickListener(this::handleClickEventTarget);
            backupPhraseTarget.setOnLongClickListener(this::handleLongClickEvent);
            backupPhraseTarget.setOnDragListener(this::handleDragEvent);
        }
    }

    private void handleClickEventSource(final View v) {
        final ShadowTextView selectedBackupPhrase = (ShadowTextView) v;

        if (selectedBackupPhrase.getText().length() == 0) {
            return;
        }

        final FlowLayout backupPhraseTargetLayout = (FlowLayout) findViewById(R.id.backup_phrase_target);
        for (int i = 0; i < backupPhraseTargetLayout.getChildCount(); i++) {
            final ShadowTextView backupPhraseTarget = (ShadowTextView) backupPhraseTargetLayout.getChildAt(i);
            if (backupPhraseTarget.getText().length() == 0) {
                final String selectedValue = selectedBackupPhrase.getText();
                final String targetValue = backupPhraseTarget.getText();

                backupPhraseTarget.setText(selectedValue);
                backupPhraseTarget.setBackgroundResource(R.drawable.background_with_radius);
                backupPhraseTarget.enableShadow();
                selectedBackupPhrase.setText(targetValue);
                selectedBackupPhrase.setBackground(null);
                selectedBackupPhrase.disableShadow();

                checkBackupPhrase();
                return;
            }
        }
    }

    private void handleClickEventTarget(final View v) {
        final ShadowTextView clickedTextView = (ShadowTextView) v;

        if (clickedTextView.getText().length() == 0) {
            return;
        }

        final FlowLayout backupPhraseSourceLayout = (FlowLayout) findViewById(R.id.backup_phrase_source);
        for (int i = 0; i < backupPhraseSourceLayout.getChildCount(); i++) {
            final ShadowTextView backupPhraseSource = (ShadowTextView) backupPhraseSourceLayout.getChildAt(i);
            if (backupPhraseSource.getText().length() == 0) {
                final String selectedValue = clickedTextView.getText();
                final String targetValue = backupPhraseSource.getText();

                clickedTextView.setBackground(null);
                clickedTextView.disableShadow();
                clickedTextView.setText(targetValue);

                backupPhraseSource.setText(selectedValue);
                backupPhraseSource.setBackgroundResource(R.drawable.background_with_radius);
                backupPhraseSource.enableShadow();

                checkBackupPhrase();
                return;
            }
        }
    }

    private boolean handleLongClickEvent(final View v) {
        this.selectedBackupPhrase = (ShadowTextView) v;
        if (this.selectedBackupPhrase.getText().length() == 0) {
            return false;
        }

        final ClipData clipData = ClipData.newPlainText("value", selectedBackupPhrase.getText());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.selectedBackupPhrase.startDragAndDrop(clipData, new View.DragShadowBuilder(v), null, 0);
        } else {
            this.selectedBackupPhrase.startDrag(clipData, new View.DragShadowBuilder(v), null, 0);
        }

        return true;
    }

    private boolean handleDragEvent(final View v, final DragEvent event) {
        final ShadowTextView targetBackupPhrase = (ShadowTextView) v;

        switch(event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                return true;

            case DragEvent.ACTION_DRAG_ENTERED:
                return true;

            case DragEvent.ACTION_DRAG_LOCATION:
                return true;

            case DragEvent.ACTION_DRAG_EXITED:
                return true;

            case DragEvent.ACTION_DROP:
                final String selectedValue = event.getClipData().getItemAt(0).getText().toString();
                final String targetValue = targetBackupPhrase.getText();

                targetBackupPhrase.setText(selectedValue);
                targetBackupPhrase.setBackgroundResource(R.drawable.background_with_radius);
                targetBackupPhrase.enableShadow();

                this.selectedBackupPhrase.setText(targetValue);
                if (this.selectedBackupPhrase.getText().length() == 0) {
                    this.selectedBackupPhrase.setBackground(null);
                }

                checkBackupPhrase();

                return true;

            case DragEvent.ACTION_DRAG_ENDED:
                return true;

            default:
                break;
        }
        return false;
    }

    public void setBackupPhrase(final List<String> backupPhrase) {
        this.backupPhrase = new ArrayList<>(backupPhrase);
        final List<String> shuffledBackupPhrase = new ArrayList<>(backupPhrase);
        Collections.shuffle(shuffledBackupPhrase);

        final FlowLayout gridLayout = (FlowLayout) findViewById(R.id.backup_phrase_source);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            final ShadowTextView backupPhraseWord = (ShadowTextView) gridLayout.getChildAt(i);
            backupPhraseWord.setText(shuffledBackupPhrase.get(i));
        }
    }

    private void checkBackupPhrase() {
        final List<String> selectedBackupPhrase = new ArrayList<>();

        final FlowLayout backupPhraseTargetLayout = (FlowLayout) findViewById(R.id.backup_phrase_target);
        for (int i = 0; i < backupPhraseTargetLayout.getChildCount(); i++) {
            final ShadowTextView backupPhraseTarget = (ShadowTextView) backupPhraseTargetLayout.getChildAt(i);
            selectedBackupPhrase.add(i, backupPhraseTarget.getText());
        }

        if (this.backupPhrase.equals(selectedBackupPhrase)) {
            this.listener.onFinish();
        }
    }
}
