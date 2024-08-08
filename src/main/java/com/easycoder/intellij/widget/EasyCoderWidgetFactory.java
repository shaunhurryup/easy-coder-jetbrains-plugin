package com.easycoder.intellij.widget;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.widget.StatusBarEditorBasedWidgetFactory;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class EasyCoderWidgetFactory extends StatusBarEditorBasedWidgetFactory {
    @Override
    public @NonNls @NotNull String getId() {
        return EasyCoderWidget.ID;
    }

    @Override
    public @Nls @NotNull String getDisplayName() {
        return "EasyCoder";
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new EasyCoderWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        Disposer.dispose(widget);
    }
}
