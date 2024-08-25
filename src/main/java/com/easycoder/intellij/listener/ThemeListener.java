package com.easycoder.intellij.listener;

import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import org.jetbrains.annotations.NotNull;
import com.google.gson.JsonObject;
import com.easycoder.intellij.enums.MessageId;
import com.easycoder.intellij.model.WebviewMessage;
import com.easycoder.intellij.services.EasyCoderSideWindowService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

import java.awt.*;

public class ThemeListener implements LafManagerListener {
    @Override
    public void lookAndFeelChanged(@NotNull LafManager source) {
        sendThemeMessage();
    }

    static public void sendThemeMessage() {
        EditorColorsScheme currentScheme = EditorColorsManager.getInstance().getGlobalScheme();
        Color backgroundColor = currentScheme.getDefaultBackground();

        JsonObject payload = new JsonObject();
        JsonObject rgb = new JsonObject();
        rgb.addProperty("r", backgroundColor.getRed());
        rgb.addProperty("g", backgroundColor.getGreen());
        rgb.addProperty("b", backgroundColor.getBlue());
        payload.add("value", rgb);
        
        WebviewMessage webviewMessage = WebviewMessage.builder()
                .id(MessageId.ToggleColorTheme)
                .payload(payload)
                .build();

        ApplicationManager.getApplication().invokeLater(() -> {
            Project[] projects = ProjectManager.getInstance().getOpenProjects();
            for (Project project : projects) {
                EasyCoderSideWindowService service = project.getService(EasyCoderSideWindowService.class);
                if (service != null) {
                    service.notifyIdeAppInstance(webviewMessage);
                }
            }
        });
    }

    public static void initialize() {
        ApplicationManager.getApplication().getMessageBus().connect()
                .subscribe(LafManagerListener.TOPIC, new ThemeListener());
    }
}