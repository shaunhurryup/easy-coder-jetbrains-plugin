package com.easycoder.intellij.actions.assistants;


import com.easycoder.intellij.services.EasyCoderSideWindowService;
import com.easycoder.intellij.window.EasyCoderSideWindow;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class OpenDevTools extends DumbAwareAction {
    public OpenDevTools() {
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            EasyCoderSideWindow easyCoderSideWindow = Objects.requireNonNull(e.getProject()).getService(EasyCoderSideWindowService.class).getEasyCoderSideWindow();
            easyCoderSideWindow.jbCefBrowser().openDevtools();
        } catch (Exception exception) {
            Logger.getInstance(this.getClass()).error("openDevtools exception", exception);
        }

    }
}