package com.easycoder.intellij.utils;

import java.util.Objects;

import com.easycoder.intellij.widget.EasyCoderWidget;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.Inlay;
import com.intellij.openapi.editor.InlayModel;
import com.intellij.openapi.vfs.VirtualFile;

public class EasyCoderUtils {
    public static String includePreText(String preText, String language, String text) {
        String sufText = "\n```" + language + "\n" + text + "\n```\n";
        return String.format(preText, language, sufText);
    }

    public static int prefixHandle(int begin, int end) {
        if (end - begin > 3000) {
            return end - 3000;
        } else {
            return begin;
        }
    }

    public static int suffixHandle(int begin, int end) {
        if (end - begin > 256) {
            return begin + 256;
        } else {
            return end;
        }
    }

    public static String getIDEVersion(String whichVersion) {
        ApplicationInfo applicationInfo = ApplicationInfo.getInstance();
        String version = "";
        try {
            if (whichVersion.equalsIgnoreCase("major")) {
                version = applicationInfo.getMajorVersion();
            } else {
                version = applicationInfo.getFullVersion();
            }
        } catch (Exception e) {
            Logger.getInstance(EasyCoderUtils.class).error("get IDE full version error", e);
        }
        return version;
    }

    public static void addCodeSuggestion(Editor focusedEditor, VirtualFile file, int suggestionPosition,
            String[] hintList) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (suggestionPosition == file.getUserData(EasyCoderWidget.EASY_CODER_POSITION)) {
                file.putUserData(EasyCoderWidget.EASY_CODER_CODE_SUGGESTION, hintList);

                InlayModel inlayModel = focusedEditor.getInlayModel();
                inlayModel.getInlineElementsInRange(0, focusedEditor.getDocument().getTextLength())
                        .forEach(EasyCoderUtils::disposeInlayHints);
                inlayModel.getBlockElementsInRange(0, focusedEditor.getDocument().getTextLength())
                        .forEach(EasyCoderUtils::disposeInlayHints);
                if (Objects.isNull(hintList) || hintList.length == 0) {
                    return;
                }
                for (int i = 0; i < hintList.length; i++) {
                    String hint = hintList[i];
                    if (hint.trim().isEmpty()) {
                        continue;
                    }
                    String[] split = hint.split("\n");
                    for (int j = 0; j < split.length; j++) {
                        String line = split[j];
                        if (j == 0) {
                            inlayModel.addInlineElement(suggestionPosition, true, new CodeGenHintRenderer(line));
                        } else {
                            inlayModel.addBlockElement(suggestionPosition, false, false, 0,
                                    new CodeGenHintRenderer(line));
                        }
                    }
                }
            }
        });
    }

    public static void disposeInlayHints(Inlay<?> inlay) {
        if (inlay.getRenderer() instanceof CodeGenHintRenderer) {
            inlay.dispose();
        }
    }

}
