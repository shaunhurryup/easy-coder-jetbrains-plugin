package com.easycoder.intellij.actions.assistants;

import java.util.Objects;
import java.util.ResourceBundle;

import org.jetbrains.annotations.NotNull;

import com.easycoder.intellij.enums.MessageId;
import com.easycoder.intellij.model.WebviewMessage;
import com.easycoder.intellij.services.EasyCoderSideWindowService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import com.intellij.DynamicBundle;

public class PerformanceCheck extends DumbAwareAction implements IntentionAction {

    private final ResourceBundle messages;

    public PerformanceCheck() {
        super(() -> ResourceBundle.getBundle("messages", DynamicBundle.getLocale()).getString("contextmenu.performance-check"));
        messages = ResourceBundle.getBundle("messages", DynamicBundle.getLocale());
    }

    @Override
    public @NotNull String getText() {
        return messages.getString("contextmenu.performance-check");
    }

    @Override
    @NotNull
    @IntentionFamilyName
    public String getFamilyName() {
        return "EasyCoder";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return false;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {

    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(LangDataKeys.PROJECT);
        ApplicationManager.getApplication().invokeLater(() -> {
            Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
            String selectedText = editor.getSelectionModel().getSelectedText();
            if (Objects.isNull(selectedText)) {
                return;
            }

            JsonObject payload = new JsonObject();
            payload.addProperty("content", selectedText);
            payload.addProperty("command", messages.getString("contextmenu.performance_check_command")); // Updated key

            WebviewMessage request = WebviewMessage.builder()
                    .id(MessageId.CheckPerformance_Menu)
                    .payload(payload)
                    .build();

            project.getService(EasyCoderSideWindowService.class).notifyIdeAppInstance(new Gson().toJson(request));
        }, ModalityState.NON_MODAL);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setText(messages.getString("contextmenu.performance-check"));
        // ... 其他更新逻辑
    }
}
