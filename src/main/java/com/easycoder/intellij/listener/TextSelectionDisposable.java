package com.easycoder.intellij.listener;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.EditorFactory;

public class TextSelectionDisposable implements Disposable {
    private final TextSelectionListener selectionListener;

    public TextSelectionDisposable(TextSelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    @Override
    public void dispose() {
        EditorFactory.getInstance().getEventMulticaster().removeSelectionListener(selectionListener);
    }
}