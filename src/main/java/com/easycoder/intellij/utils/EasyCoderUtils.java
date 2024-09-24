package com.easycoder.intellij.utils;

import com.easycoder.intellij.enums.ServiceRoute;
import com.easycoder.intellij.http.HttpToolkits; // Add this import
import com.easycoder.intellij.model.CompletionResult;
import com.easycoder.intellij.model.WebviewMessage; // Add this import
import com.easycoder.intellij.widget.EasyCoderWidget;
import com.google.gson.JsonObject; // Add this import
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

    public static void addCodeSuggestion(Editor focusedEditor, VirtualFile file, int suggestionPosition, CompletionResult[] completionResult) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (suggestionPosition == file.getUserData(EasyCoderWidget.EASY_CODER_POSITION)) {
                String[] generatedTexts = new String[completionResult.length];
                for (int i = 0; i < completionResult.length; i++) {
                    generatedTexts[i] = completionResult[i].getGeneratedText();
                }
                file.putUserData(EasyCoderWidget.EASY_CODER_CODE_SUGGESTION, generatedTexts);

                InlayModel inlayModel = focusedEditor.getInlayModel();
                inlayModel.getInlineElementsInRange(0, focusedEditor.getDocument().getTextLength())
                        .forEach(EasyCoderUtils::disposeInlayHints);
                inlayModel.getBlockElementsInRange(0, focusedEditor.getDocument().getTextLength())
                        .forEach(EasyCoderUtils::disposeInlayHints);
                if (generatedTexts.length == 0) {
                    return;
                }
                for (int j = 0; j < generatedTexts.length; j++) {
                    String hint = generatedTexts[j];
                    if (hint.trim().isEmpty()) {
                        continue;
                    }
                    String[] split = hint.split("\n");
                    if (j == 0) {
                        inlayModel.addInlineElement(suggestionPosition, true, new CodeGenHintRenderer(split));
                    } else {
                        inlayModel.addBlockElement(suggestionPosition, false, false, 0,
                                new CodeGenHintRenderer(split));
                    }
                }
            }
        });

        // Add tracking event in a separate invokeLater block
        ApplicationManager.getApplication().invokeLater(() -> {
            String recordId = completionResult[0].getRecordId();
            if (recordId == null) {
                return;
            }
            JsonObject payload = new JsonObject();
            payload.addProperty("route", ServiceRoute.ACCEPT_INLINE_COMPLETION.getRoute() + completionResult[0].getRecordId());
            WebviewMessage trackingMessage = WebviewMessage.builder()
                    .payload(payload)
                    .build();
            HttpToolkits.doHttpGet(trackingMessage);
        });
    }

    public static void disposeInlayHints(Inlay<?> inlay) {
        if (inlay.getRenderer() instanceof CodeGenHintRenderer) {
            inlay.dispose();
        }
    }

}
