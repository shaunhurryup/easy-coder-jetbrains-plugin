package com.easycoder.intellij.listener;

import com.easycoder.intellij.enums.MessageId;
import com.easycoder.intellij.model.WebviewMessage;
import com.easycoder.intellij.services.EasyCoderSideWindowService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class TextSelectionListener implements SelectionListener {
    private final Project project;

    public TextSelectionListener(Project project) {
        this.project = project;
    }

    @Override
    public void selectionChanged(@NotNull SelectionEvent e) {
        Editor editor = e.getEditor();
        SelectionModel selectionModel = editor.getSelectionModel();
        String selectedText = selectionModel.getSelectedText();

        JsonObject payload = new JsonObject();
        payload.addProperty("value", selectedText);
        WebviewMessage webviewMessage = WebviewMessage.builder()
            .id(MessageId.SetSelectedText)
            .payload(payload)
            .build();
        project.getService(EasyCoderSideWindowService.class).notifyIdeAppInstance(new Gson().toJson(webviewMessage));
    }
}

