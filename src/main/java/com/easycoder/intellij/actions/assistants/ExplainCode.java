package com.easycoder.intellij.actions.assistants;

import com.easycoder.intellij.enums.MessageId;
import com.easycoder.intellij.model.WebviewMessage;
import com.easycoder.intellij.services.EasyCoderSideWindowService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ExplainCode extends DumbAwareAction implements IntentionAction {

    @SafeFieldForPreview
    private Logger logger = Logger.getInstance(this.getClass());

    @Override
    @IntentionName
    @NotNull
    public String getText() {
        return "Explain Code";
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
            payload.addProperty("command", "帮我解释这段代码");

            WebviewMessage request = WebviewMessage.builder()
                    .id(MessageId.ExplainCode_Menu)
                    .payload(payload)
                    .build();

            project.getService(EasyCoderSideWindowService.class).notifyIdeAppInstance(new Gson().toJson(request));
        }, ModalityState.NON_MODAL);
    }
}
