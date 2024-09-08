package com.easycoder.intellij.handlers;

import java.awt.datatransfer.StringSelection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import com.easycoder.intellij.enums.MessageId;
import com.easycoder.intellij.http.HttpToolkits;
import com.easycoder.intellij.listener.ThemeListener;
import com.easycoder.intellij.model.WebviewMessage;
import com.easycoder.intellij.services.EasyCoderSideWindowService;
import com.easycoder.intellij.settings.EasyCoderSettings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;

import cn.hutool.core.util.ObjectUtil;

public class WebviewMessageHandler {
    private static final AtomicReference<Runnable> lastAbortFunction = new AtomicReference<>();

    public static WebviewMessage run(WebviewMessage message, Project project) {
        MessageId messageId = message.getId();

        if (messageId.equals(MessageId.InsertIntoEditor)) {
            String value = message.getPayload().get("value").getAsString();
            insertText(project, value);
        }

        if (messageId.equals(MessageId.GetHistoryDialogs)) {
            String modifiedStream = HttpToolkits.doHttpPost(message);
            sendHttpResponse2Webview(modifiedStream, messageId, project);
        }

        if (messageId.equals(MessageId.OpenExternalLink)) {
            String url = message.getPayload().get("url").getAsString();
            BrowserUtil.browse(url);
        }

        if (messageId.equals(MessageId.ClearSelectedText)) {
            removeSelectedText(project);
        }

        if (messageId.equals(MessageId.WebviewCommandQuestion)) {
            String selectedText = getSelectedText(project);
            if (selectedText == null || selectedText.isEmpty()) {
                JsonObject payload = new JsonObject();
                payload.addProperty("value", "Please select some code first");
                WebviewMessage webviewMessage = WebviewMessage.builder()
                    .id(MessageId.ToastWarning)
                    .payload(payload)
                    .build();
                project.getService(EasyCoderSideWindowService.class)
                    .notifyIdeAppInstance(new Gson().toJson(webviewMessage));
                return null;
            }

            JsonObject payload = new JsonObject();
            payload.addProperty("code", selectedText);
            payload.add("action", message.getPayload().get("action"));
            WebviewMessage webviewMessage = WebviewMessage.builder()
                .id(MessageId.WebviewCommandQuestion)
                .payload(payload)
                .build();
            project.getService(EasyCoderSideWindowService.class)
                .notifyIdeAppInstance(new Gson().toJson(webviewMessage));
        }

        if (messageId.equals(MessageId.CopyToClipboard)) {
            String text = message.getPayload().get("value").getAsString();
            ApplicationManager.getApplication().invokeLater(() -> {
                // 使用 IntelliJ Platform 提供的工具类将文本复制到剪贴板
                CopyPasteManager.getInstance().setContents(new StringSelection(text));
            });
        }

        if (
            messageId.equals(MessageId.WebviewQuestion) ||
            messageId.equals(MessageId.GenerateComment_Menu) ||
            messageId.equals(MessageId.ReGenerateAnswer) ||
            messageId.equals(MessageId.WebviewCodeTranslation)
        ) {
            Runnable abortFunction = HttpToolkits.createEventSource(message, project);
            lastAbortFunction.set(abortFunction);
        }

        if (messageId.equals(MessageId.WebviewAbortQa)) {
            Runnable abortFunction = lastAbortFunction.getAndSet(null);
            if (abortFunction != null) {
                abortFunction.run();
            }
        }

        if (messageId.equals(MessageId.OpenSignInWebpage)) {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    String uuid = UUID.randomUUID().toString();
                    String targetUrl = EasyCoderSettings.getInstance().getServerAddressShaun() + "?sessionId=" + uuid;
                    BrowserUtil.browse(targetUrl);
                    CompletableFuture<Map<String, String>> pollingFuture = HttpToolkits.fetchToken(uuid);
                    Map<String, String> data = pollingFuture.get();

                    // 4) Update accounts
                    if (data == null) {
                        System.err.println("[EasyCoder]");
                        return;
                    }

                    PropertiesComponent.getInstance().setValue("easycoder:token", data.get("token"));
                    PropertiesComponent.getInstance().setValue("easycoder:username", data.get("username"));
                    PropertiesComponent.getInstance().setValue("easycoder:userId", data.get("userId"));

                    JsonObject payload = new JsonObject();
                    payload.addProperty("account", data.get("username"));
                    payload.addProperty("accessToken", data.get("token"));

                    WebviewMessage webviewMessage = WebviewMessage.builder()
                        .id(MessageId.SuccessfulAuth)
                        .payload(payload)
                        .build();
                    project.getService(EasyCoderSideWindowService.class)
                        .notifyIdeAppInstance(new Gson().toJson(webviewMessage));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        if (messageId.equals(MessageId.WebviewInitQaExamples) || messageId.equals(MessageId.GetHistoryDialogDetail) || messageId.equals(MessageId.RemoveDialog32)) {
            String modifiedStream = HttpToolkits.doHttpGet(message);
            sendHttpResponse2Webview(modifiedStream, messageId, project);
        }

        if (messageId.equals(MessageId.WebviewMount)) {
            // language
            project.getService(EasyCoderSideWindowService.class)
                .notifyIdeAppInstance(new Gson().toJson(EasyCoderSettings.getInstance().getSettings()));

            String token = PropertiesComponent.getInstance().getValue("easycoder:token");
            String username = PropertiesComponent.getInstance().getValue("easycoder:username");
            if (ObjectUtil.isEmpty(token) || ObjectUtil.isEmpty(username)) {
                System.out.println("[WARN] token or username is empty");
                return null;
            }

            // auth
            JsonObject payload = new JsonObject();
            payload.addProperty("account", username);
            payload.addProperty("accessToken", token);
            WebviewMessage webviewMessage = WebviewMessage.builder()
                .id(MessageId.SuccessfulAuth)
                .payload(payload)
                .build();
            project.getService(EasyCoderSideWindowService.class)
                .notifyIdeAppInstance(webviewMessage);

            // theme
            ThemeListener.sendThemeMessage();
        }

        if (messageId.equals(MessageId.ReplaceEditorText)) {
            ApplicationManager.getApplication().invokeLater(() -> {
                FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                Editor editor = fileEditorManager.getSelectedTextEditor();

                if (editor == null) {
                    // 可以根据需要替换 VscodeMessage.error 方法
                    System.err.println("Expect editor to be active, but got null");
                    return;
                }

                SelectionModel selectionModel = editor.getSelectionModel();
                Document document = editor.getDocument();

                WriteCommandAction.runWriteCommandAction(project, () -> {
                    // 先留着
                    //                isProgrammaticChange = true;

                    if (selectionModel.hasSelection()) {
                        // 如果有选中内容，则替换选中内容
                        document.replaceString(selectionModel.getSelectionStart(), selectionModel.getSelectionEnd(), message.getPayload().get("value").getAsString());
                    }
                    // 先留着
//                else if (insert) {
//                    // 如果没有选中内容，并且 insert 为 true，则插入新内容
//                    document.insertString(selectionModel.getSelectionStart(), text);
//                }

//                ApplicationManager.getApplication().invokeLater(() -> isProgrammaticChange = false);
                });

            });
        }

        return null;
    }

    static public String getSelectedText(Project project) {
        final String[] selectedText = {""};

        ApplicationManager.getApplication().invokeLater(() -> {
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            Editor editor = fileEditorManager.getSelectedTextEditor();
            if (editor != null) {
                selectedText[0] = editor.getSelectionModel().getSelectedText();
            }
        });

        ApplicationManager.getApplication().invokeAndWait(() -> {
        });
        return selectedText[0];
    }

    static public void removeSelectedText(Project project) {
        ApplicationManager.getApplication().invokeLater(() -> {
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            Editor editor = fileEditorManager.getSelectedTextEditor();
            if (editor != null) {
                editor.getSelectionModel().removeSelection();
            }
        });
    }

    static public void insertText(Project project, String text) {
        ApplicationManager.getApplication().invokeLater(() -> {
            FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
            Editor editor = fileEditorManager.getSelectedTextEditor();
            if (editor != null) {
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    editor.getDocument().insertString(editor.getCaretModel().getOffset(), text);
                });
            }
        });
    }

    static public void sendHttpResponse2Webview(String responseBody, MessageId messageId, Project project) {
        if (responseBody == null) {
            return;
        }

        JsonObject payload = new JsonObject();
        // 直接传 string 嵌套的 json 不会被 webview 端解析
        // 如果要保留完整的 json 需要转换
        payload.add("$response", new Gson().fromJson(responseBody, JsonObject.class));
        WebviewMessage webviewMessage = WebviewMessage.builder()
            .id(messageId)
            .payload(payload)
            .build();
        project.getService(EasyCoderSideWindowService.class)
            .notifyIdeAppInstance(new Gson().toJson(webviewMessage));
    }
}