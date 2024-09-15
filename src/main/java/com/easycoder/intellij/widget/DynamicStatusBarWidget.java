package com.easycoder.intellij.widget;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;

public class DynamicStatusBarWidget implements StatusBarWidget, StatusBarWidget.TextPresentation {
    private final Project project;
    private String text = "Initial Status";

    public DynamicStatusBarWidget(Project project) {
        this.project = project;
    }

    @NotNull
    @Override
    public String ID() {
        return "DynamicStatusBarWidget";
    }

    @Nullable
    @Override
    public WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
    }

    @Override
    public void dispose() {
    }

    @NotNull
    @Override
    public String getText() {
        return text;
    }

    @Nullable
    @Override
    public String getTooltipText() {
        return "Dynamic Status Bar Widget";
    }

    @Nullable
    @Override
    public Consumer<MouseEvent> getClickConsumer() {
        return null;
    }

    public void updateText(String newText) {
        this.text = newText;
        if (project != null && !project.isDisposed()) {
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
            if (statusBar != null) {
                statusBar.updateWidget(ID());
            }
        }
    }

    @Override
    public float getAlignment() {
        return 0L;
    }
}
