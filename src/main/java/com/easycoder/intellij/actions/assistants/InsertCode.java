package com.easycoder.intellij.actions.assistants;

import com.easycoder.intellij.constant.PrefixString;
import com.easycoder.intellij.services.EasyCoderSideWindowService;
import com.easycoder.intellij.utils.EditorUtils;
import com.google.gson.JsonObject;
import com.intellij.codeInsight.intention.FileModifier;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class InsertCode extends AnAction {

    @FileModifier.SafeFieldForPreview
    private Logger logger = Logger.getInstance(this.getClass());

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(LangDataKeys.PROJECT);
        if (Objects.isNull(project)) {
            return;
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            VirtualFile vf = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE);
            Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
            if (EditorUtils.isNoneTextSelected(editor)) {
                return;
            }
            PsiFile psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE);
            JsonObject jsonObject = EditorUtils.getFileSelectionDetails(editor, psiFile, true, PrefixString.COMMENT_CODE);
            JsonObject result = new JsonObject();
            ToolWindowManager tool = ToolWindowManager.getInstance(project);
            Objects.requireNonNull(tool.getToolWindow("EasyCoder")).activate(() -> {
                if(logger.isDebugEnabled()){
                    logger.debug("******************* InsertCode Enabled EasyCoder window *******************");
                }
            }, true, true);
            jsonObject.addProperty("fileName", vf.getName());
            jsonObject.addProperty("filePath", vf.getCanonicalPath());
            result.addProperty("data", jsonObject.toString());
            (project.getService(EasyCoderSideWindowService.class)).notifyIdeAppInstance(result);
        }, ModalityState.NON_MODAL);
    }
}
