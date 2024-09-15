package com.easycoder.intellij.actions.complete;

import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.easycoder.intellij.services.EasyCoderCompleteService;
import com.easycoder.intellij.settings.EasyCoderSettings;
import com.easycoder.intellij.utils.EasyCoderUtils;
import com.easycoder.intellij.utils.EditorUtils;
import com.easycoder.intellij.widget.EasyCoderWidget;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.InlayModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

import com.intellij.DynamicBundle;

public class CodeTriggerCompletionAction extends DumbAwareAction implements IntentionAction {

	private final ResourceBundle messages;
	private boolean completionEnabled = false;

	public CodeTriggerCompletionAction() {
		super(() -> ResourceBundle.getBundle("messages", DynamicBundle.getLocale()).getString("contextmenu.trigger-completion"));
		messages = ResourceBundle.getBundle("messages", DynamicBundle.getLocale());
	}

	@Override
	@IntentionName
	@NotNull
	public String getText() {
		return completionEnabled ? messages.getString("contextmenu.disable-completion") : messages.getString("contextmenu.trigger-completion");
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
		completionEnabled = !completionEnabled;
		if (completionEnabled) {
			Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
			Project project = e.getData(CommonDataKeys.PROJECT);
			CompletableFuture.delayedExecutor(EasyCoderSettings.getInstance().getCodeCompletionDelayShaun().getValue(), TimeUnit.MILLISECONDS).execute(() -> updateInlayHints(editor, project));
		} else {
			// 禁用补全功能的逻辑,例如清除所有的InlayHint
			Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
			InlayModel inlayModel = editor.getInlayModel();
			inlayModel.getInlineElementsInRange(0, editor.getDocument().getTextLength()).forEach(EasyCoderUtils::disposeInlayHints);
			inlayModel.getBlockElementsInRange(0, editor.getDocument().getTextLength()).forEach(EasyCoderUtils::disposeInlayHints);
		}
	}

	private void updateInlayHints(Editor focusedEditor, Project project) {
		boolean enableCodeCompletionShaun = EasyCoderSettings.getInstance().isEnableCodeCompletionShaun();
		String token = PropertiesComponent.getInstance().getValue("easycoder:token");
		if (StringUtils.isBlank(token) || !enableCodeCompletionShaun || Objects.isNull(focusedEditor) || !EditorUtils.isMainEditor(focusedEditor)) {
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
		CompletableFuture<String[]> future = CompletableFuture.supplyAsync(() -> easyCoder.getCodeCompletionHints(editorContents, currentPosition, project));
		future.thenAccept(hintList -> EasyCoderUtils.addCodeSuggestion(focusedEditor, file, currentPosition, hintList));
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setText(completionEnabled ? messages.getString("contextmenu.disable-completion") : messages.getString("contextmenu.trigger-completion"));
		// ... 其他更新逻辑
	}
}
