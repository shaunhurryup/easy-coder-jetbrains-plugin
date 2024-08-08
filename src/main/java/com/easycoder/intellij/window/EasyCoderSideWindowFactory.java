package com.easycoder.intellij.window;

import com.easycoder.intellij.services.EasyCoderSideWindowService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.jcef.JBCefApp;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class EasyCoderSideWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();
        JPanel webPanel = new JPanel(new BorderLayout());
        EasyCoderSideWindowService service = project.getService(EasyCoderSideWindowService.class);
        if (Objects.nonNull(service.getEasyCoderSideWindow()) && Objects.nonNull(service.getEasyCoderSideWindow().getComponent())) {
            webPanel.add(service.getEasyCoderSideWindow().getComponent());
            Content labelContent = contentManager.getFactory().createContent(webPanel, "", false);
            contentManager.addContent(labelContent);
        }
    }

    static {
        JBCefApp.getInstance();
    }

}
