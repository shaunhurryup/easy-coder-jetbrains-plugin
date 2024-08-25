package com.easycoder.intellij.widget;

import com.easycoder.intellij.enums.EasyCoderStatus;
import com.easycoder.intellij.services.EasyCoderCompleteService;
import com.easycoder.intellij.settings.EasyCoderSettings;
import com.easycoder.intellij.utils.CodeGenHintRenderer;
import com.easycoder.intellij.utils.EasyCoderIcons;
import com.easycoder.intellij.utils.EasyCoderUtils;
import com.easycoder.intellij.utils.EditorUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.event.*;
import com.intellij.openapi.editor.impl.EditorComponentImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedWidget;
import com.intellij.openapi.wm.impl.status.TextPanel;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class EasyCoderWidget extends EditorBasedWidget
        implements StatusBarWidget.Multiframe, StatusBarWidget.IconPresentation,
        CaretListener, SelectionListener, BulkAwareDocumentListener.Simple, PropertyChangeListener {
    public static final String ID = "EasyCoderWidget";

    public static final Key<String[]> EASY_CODER_CODE_SUGGESTION = new Key<>("EasyCoder Code Suggestion");
    public static final Key<Integer> EASY_CODER_POSITION = new Key<>("EasyCoder Position");
    public static boolean enableSuggestion = false;
    private final TextPanel.WithIconAndArrows panel = new TextPanel.WithIconAndArrows();



    protected EasyCoderWidget(@NotNull Project project) {
        super(project);
    }

    @Override
    public @NonNls @NotNull String ID() {
        return ID;
    }

    @Override
    public StatusBarWidget copy() {
        return new EasyCoderWidget(getProject());
    }

    @Override
    public @Nullable Icon getIcon() {
        EasyCoderCompleteService easyCoder = ApplicationManager.getApplication().getService(EasyCoderCompleteService.class);
        EasyCoderStatus status = EasyCoderStatus.getStatusByCode(easyCoder.getStatus());
        if (status == EasyCoderStatus.OK) {
            return EasyCoderSettings.getInstance().isSaytEnabled() ? EasyCoderIcons.WidgetEnabled : EasyCoderIcons.WidgetDisabled;
        } else {
            return EasyCoderIcons.WidgetError;
        }
    }

    @Override
    public WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public @Nullable @NlsContexts.Tooltip String getTooltipText() {
        StringBuilder toolTipText = new StringBuilder("EasyCoder");
        if (EasyCoderSettings.getInstance().isSaytEnabled()) {
            toolTipText.append(" enabled");
        } else {
            toolTipText.append(" disabled");
        }

        EasyCoderCompleteService easyCoder = ApplicationManager.getApplication().getService(EasyCoderCompleteService.class);
        int statusCode = easyCoder.getStatus();
        EasyCoderStatus status = EasyCoderStatus.getStatusByCode(statusCode);
        switch (status) {
            case OK:
                if (EasyCoderSettings.getInstance().isSaytEnabled()) {
                    toolTipText.append(" (Click to disable)");
                } else {
                    toolTipText.append(" (Click to enable)");
                }
                break;
            case UNKNOWN:
                toolTipText.append(" (http error ");
                toolTipText.append(statusCode);
                toolTipText.append(")");
                break;
            default:
                toolTipText.append(" (");
                toolTipText.append(status.getDisplayValue());
                toolTipText.append(")");
        }

        return toolTipText.toString();
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return mouseEvent -> {
            EasyCoderSettings.getInstance().toggleSaytEnabled();
            if (Objects.nonNull(myStatusBar)) {
                myStatusBar.updateWidget(ID);
            }
        };
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        super.install(statusBar);
        EditorEventMulticaster multicaster = EditorFactory.getInstance().getEventMulticaster();
        multicaster.addCaretListener(this, this);
        multicaster.addSelectionListener(this, this);
        multicaster.addDocumentListener(this, this);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner", this);
        Disposer.register(this,
                () -> KeyboardFocusManager.getCurrentKeyboardFocusManager().removePropertyChangeListener("focusOwner",
                        this)
        );
    }

    private Editor getFocusOwnerEditor() {
        Component component = getFocusOwnerComponent();
        Editor editor = component instanceof EditorComponentImpl ? ((EditorComponentImpl) component).getEditor() : getEditor();
        return Objects.nonNull(editor) && !editor.isDisposed() && EditorUtils.isMainEditor(editor) ? editor : null;
    }

    private Component getFocusOwnerComponent() {
        Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        if (Objects.isNull(focusOwner)) {
            IdeFocusManager focusManager = IdeFocusManager.getInstance(getProject());
            Window frame = focusManager.getLastFocusedIdeWindow();
            if (Objects.nonNull(frame)) {
                focusOwner = focusManager.getLastFocusedFor(frame);
            }
        }
        return focusOwner;
    }

    private boolean isFocusedEditor(Editor editor) {
        Component focusOwner = getFocusOwnerComponent();
        return focusOwner == editor.getContentComponent();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        updateInlayHints(getFocusOwnerEditor());
    }

    @Override
    public void selectionChanged(SelectionEvent event) {
        updateInlayHints(event.getEditor());
    }

    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        updateInlayHints(event.getEditor());
    }

    @Override
    public void caretAdded(@NotNull CaretEvent event) {
        updateInlayHints(event.getEditor());
    }

    @Override
    public void caretRemoved(@NotNull CaretEvent event) {
        updateInlayHints(event.getEditor());
    }

    @Override
    public void afterDocumentChange(@NotNull Document document) {
        enableSuggestion = true;
        if (ApplicationManager.getApplication().isDispatchThread()) {
            EditorFactory.getInstance().editors(document)
                    .filter(this::isFocusedEditor)
                    .findFirst()
                    .ifPresent(this::updateInlayHints);
        }
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
            String[] existingHints = file.getUserData(EASY_CODER_CODE_SUGGESTION);
            if (Objects.nonNull(existingHints) && existingHints.length > 0) {
                file.putUserData(EASY_CODER_CODE_SUGGESTION, null);
                file.putUserData(EASY_CODER_POSITION, focusedEditor.getCaretModel().getOffset());

                InlayModel inlayModel = focusedEditor.getInlayModel();
                inlayModel.getInlineElementsInRange(0, focusedEditor.getDocument().getTextLength()).forEach(EasyCoderUtils::disposeInlayHints);
                inlayModel.getBlockElementsInRange(0, focusedEditor.getDocument().getTextLength()).forEach(EasyCoderUtils::disposeInlayHints);
            }
            return;
        }

        Integer easyCoderPos = file.getUserData(EASY_CODER_POSITION);
        int lastPosition = (Objects.isNull(easyCoderPos)) ? 0 : easyCoderPos;
        int currentPosition = focusedEditor.getCaretModel().getOffset();

        if (lastPosition == currentPosition) return;

        InlayModel inlayModel = focusedEditor.getInlayModel();
        if (currentPosition > lastPosition) {
            String[] existingHints = file.getUserData(EASY_CODER_CODE_SUGGESTION);
            if (Objects.nonNull(existingHints) && existingHints.length > 0) {
                String inlineHint = existingHints[0];
                String modifiedText = focusedEditor.getDocument().getCharsSequence().subSequence(lastPosition, currentPosition).toString();
                if (modifiedText.startsWith("\n")) {
                    modifiedText = modifiedText.replace(" ", "");
                }
                if (inlineHint.startsWith(modifiedText)) {
                    inlineHint = inlineHint.substring(modifiedText.length());
                    enableSuggestion = false;
                    if (inlineHint.length() > 0) {
                        inlayModel.getInlineElementsInRange(0, focusedEditor.getDocument().getTextLength()).forEach(EasyCoderUtils::disposeInlayHints);
                        inlayModel.addInlineElement(currentPosition, true, new CodeGenHintRenderer(inlineHint));
                        existingHints[0] = inlineHint;

                        file.putUserData(EASY_CODER_CODE_SUGGESTION, existingHints);
                        file.putUserData(EASY_CODER_POSITION, currentPosition);
                        return;
                    } else if (existingHints.length > 1) {
                        existingHints = Arrays.copyOfRange(existingHints, 1, existingHints.length);
                        EasyCoderUtils.addCodeSuggestion(focusedEditor, file, currentPosition, existingHints);
                        return;
                    } else {
                        file.putUserData(EASY_CODER_CODE_SUGGESTION, null);
                    }
                }
            }
        }

        inlayModel.getInlineElementsInRange(0, focusedEditor.getDocument().getTextLength()).forEach(EasyCoderUtils::disposeInlayHints);
        inlayModel.getBlockElementsInRange(0, focusedEditor.getDocument().getTextLength()).forEach(EasyCoderUtils::disposeInlayHints);

        file.putUserData(EASY_CODER_POSITION, currentPosition);
        // fixed: 删除也要有代码补全
        // if(!enableSuggestion || currentPosition < lastPosition){
        if(!enableSuggestion){
            enableSuggestion = false;
            return;
        }
        EasyCoderCompleteService easyCoder = ApplicationManager.getApplication().getService(EasyCoderCompleteService.class);
        CharSequence editorContents = focusedEditor.getDocument().getCharsSequence();
        CompletableFuture<String[]> future = CompletableFuture.supplyAsync(() -> easyCoder.getCodeCompletionHints(editorContents, currentPosition));
        future.thenAccept(hintList -> EasyCoderUtils.addCodeSuggestion(focusedEditor, file, currentPosition, hintList));
    }

}
