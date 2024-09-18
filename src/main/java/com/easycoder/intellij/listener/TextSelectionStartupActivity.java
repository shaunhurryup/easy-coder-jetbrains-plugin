package com.easycoder.intellij.listener;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.editor.EditorFactory;
import org.jetbrains.annotations.NotNull;

public class TextSelectionStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        TextSelectionListener selectionListener = new TextSelectionListener(project);
        TextSelectionDisposable disposable = new TextSelectionDisposable(selectionListener);
        EditorFactory.getInstance().getEventMulticaster().addSelectionListener(selectionListener, disposable);
    }
}