package com.easycoder.intellij.listener;

import com.intellij.openapi.project.Project;

import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.editor.EditorFactory;
import org.jetbrains.annotations.Nullable;

public class TextSelectionStartupActivity implements ProjectActivity {
    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        TextSelectionListener selectionListener = new TextSelectionListener(project);
        TextSelectionDisposable disposable = new TextSelectionDisposable(selectionListener);
        EditorFactory.getInstance().getEventMulticaster().addSelectionListener(selectionListener, disposable);
        
        return null;
    }
}