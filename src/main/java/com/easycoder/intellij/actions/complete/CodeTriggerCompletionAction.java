package com.easycoder.intellij.actions.complete;

import com.easycoder.intellij.services.EasyCoderCompleteService;
import com.easycoder.intellij.utils.EasyCoderUtils;
import com.easycoder.intellij.utils.EditorUtils;
import com.easycoder.intellij.widget.EasyCoderWidget;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.InlayModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class CodeTriggerCompletionAction extends DumbAwareAction implements IntentionAction {

    @SafeFieldForPreview
    private Logger logger = Logger.getInstance(this.getClass());

    @Override
    @IntentionName
    @NotNull
    public String getText() {
        return "Trigger Completion";
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
        Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
        updateInlayHints(editor);
//        Project project = e.getData(LangDataKeys.PROJECT);
//        if (Objects.isNull(project)) {
//            return;
//        }
//        ApplicationManager.getApplication().invokeLater(() -> {
//            VirtualFile vf = e.getRequiredData(CommonDataKeys.VIRTUAL_FILE);
//            Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
//            if (EditorUtils.isNoneTextSelected(editor)) {
//                return;
//            }
//            PsiFile psiFile = e.getRequiredData(CommonDataKeys.PSI_FILE);
//            JsonObject jsonObject = EditorUtils.getFileSelectionDetails(editor, psiFile, true, PrefixString.CLEAN_CODE);
//            JsonObject result = new JsonObject();
//            ToolWindowManager tool = ToolWindowManager.getInstance(project);
//            Objects.requireNonNull(tool.getToolWindow("EasyCoder")).activate(() -> {
//                if(logger.isDebugEnabled()){
//                    logger.debug("******************* CleanCode Enabled EasyCoder window *******************");
//                }
//            }, true, true);
//            jsonObject.addProperty("fileName", vf.getName());
//            jsonObject.addProperty("filePath", vf.getCanonicalPath());
//            result.addProperty("data", jsonObject.toString());
//            (project.getService(EasyCoderSideWindowService.class)).notifyIdeAppInstance(result);
//        }, ModalityState.NON_MODAL);
    }

    private void updateInlayHints(Editor focusedEditor) {
        if (Objects.isNull(focusedEditor) || !EditorUtils.isMainEditor(focusedEditor)) {
            return;
        }
        VirtualFile file = FileDocumentManager.getInstance().getFile(focusedEditor.getDocument());
        if (Objects.isNull(file)) {
            return;
        }

        String selection = focusedEditor.getCaretModel().getCurrentCaret().getSelectedText();
        if (Objects.nonNull(selection) && !selection.isEmpty()) {
            String[] existingHints = file.getUserData(EasyCoderWidget.EASY_CODER_CODE_SUGGESTION);
            if (Objects.nonNull(existingHints) && existingHints.length > 0) {
                file.putUserData(EasyCoderWidget.EASY_CODER_CODE_SUGGESTION, null);
                file.putUserData(EasyCoderWidget.EASY_CODER_POSITION, focusedEditor.getCaretModel().getOffset());

                InlayModel inlayModel = focusedEditor.getInlayModel();
                inlayModel.getInlineElementsInRange(0, focusedEditor.getDocument().getTextLength()).forEach(EasyCoderUtils::disposeInlayHints);
                inlayModel.getBlockElementsInRange(0, focusedEditor.getDocument().getTextLength()).forEach(EasyCoderUtils::disposeInlayHints);
            }
            return;
        }

        Integer easyCoderPos = file.getUserData(EasyCoderWidget.EASY_CODER_POSITION);
        int currentPosition = focusedEditor.getCaretModel().getOffset();

        InlayModel inlayModel = focusedEditor.getInlayModel();
        inlayModel.getInlineElementsInRange(0, focusedEditor.getDocument().getTextLength()).forEach(EasyCoderUtils::disposeInlayHints);
        inlayModel.getBlockElementsInRange(0, focusedEditor.getDocument().getTextLength()).forEach(EasyCoderUtils::disposeInlayHints);
        file.putUserData(EasyCoderWidget.EASY_CODER_POSITION, currentPosition);
        EasyCoderCompleteService easyCoder = ApplicationManager.getApplication().getService(EasyCoderCompleteService.class);
        CharSequence editorContents = focusedEditor.getDocument().getCharsSequence();
        CompletableFuture<String[]> future = CompletableFuture.supplyAsync(() -> easyCoder.getCodeCompletionHints(editorContents, currentPosition));
        future.thenAccept(hintList -> EasyCoderUtils.addCodeSuggestion(focusedEditor, file, currentPosition, hintList));
    }
}
