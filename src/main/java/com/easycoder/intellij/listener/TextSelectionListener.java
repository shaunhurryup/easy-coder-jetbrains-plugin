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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class TextSelectionListener implements SelectionListener {
    private final Project project;
    private final AtomicBoolean isDisposed = new AtomicBoolean(false);

    public TextSelectionListener(Project project) {
        this.project = project;

        Disposer.register(project, () -> isDisposed.set(true));
    }

    @Override
    public void selectionChanged(@NotNull SelectionEvent e) {
        if (isDisposed.get() || project.isDisposed()) {
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            if (isDisposed.get() || project.isDisposed()) {
                return;
            }

            Editor editor = e.getEditor();
            SelectionModel selectionModel = editor.getSelectionModel();
            String selectedText = selectionModel.getSelectedText();

            JsonObject payload = new JsonObject();
            payload.addProperty("value", selectedText == null ? "" : selectedText);
            WebviewMessage webviewMessage = WebviewMessage.builder()
                .id(MessageId.SetSelectedText)
                .payload(payload)
                .build();

            EasyCoderSideWindowService service = project.getService(EasyCoderSideWindowService.class);
            if (service != null) {
                service.notifyIdeAppInstance(new Gson().toJson(webviewMessage));
            }
        });
    }
}