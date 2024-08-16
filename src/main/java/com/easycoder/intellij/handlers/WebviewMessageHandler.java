package com.easycoder.intellij.handlers;

import com.easycoder.intellij.constant.HttpRequest;
import com.easycoder.intellij.enums.MessageId;
import com.easycoder.intellij.http.HttpToolkits;
import com.easycoder.intellij.model.WebviewMessage;
import com.easycoder.intellij.services.EasyCoderSideWindowService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WebviewMessageHandler {
    public static WebviewMessage run(WebviewMessage message, Project project) {
        MessageId messageId = message.getId();

        if (messageId.equals(MessageId.WebviewQuestion)) {
            HttpToolkits.createEventSource(message, project);
        }

        if (messageId.equals(MessageId.OpenSignInWebpage)) {
            try {
                String uuid = UUID.randomUUID().toString();
                String targetUrl = HttpRequest.baseURL + "?sessionId=" + uuid;
                BrowserUtil.browse(targetUrl);
                CompletableFuture<Map<String, String>> pollingFuture = HttpToolkits.fetchToken(uuid);
                Map<String, String> data = pollingFuture.get(); // This will block until the polling is done

                // 4) Update accounts
                if (data == null) {
                    System.err.println("[EasyCoder]");
                    return null;
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
        }
        
        if (messageId.equals(MessageId.WebviewInitQaExamples)) {
            String modifiedStream = HttpToolkits.doHttpGet(message);
            if (modifiedStream == null) {
                return null;
            }

            JsonObject payload = new JsonObject();
            // 直接传 string 嵌套的 json 不会被 webview 端解析
            // 如果要保留完整的 json 需要转换
            payload.add("$response", new Gson().fromJson(modifiedStream, JsonObject.class));
            WebviewMessage webviewMessage = WebviewMessage.builder()
                    .id(messageId)
                    .payload(payload)
                    .build();
            project.getService(EasyCoderSideWindowService.class)
                    .notifyIdeAppInstance(new Gson().toJson(webviewMessage));
        }

        if (messageId.equals(MessageId.WebviewMount)) {
            String token = PropertiesComponent.getInstance().getValue("easycoder:token");
            String username = PropertiesComponent.getInstance().getValue("easycoder:username");
            JsonObject payload = new JsonObject();
            payload.addProperty("account", username);
            payload.addProperty("accessToken", token);

            WebviewMessage webviewMessage = WebviewMessage.builder()
                    .id(MessageId.SuccessfulAuth)
                    .payload(payload)
                    .build();
            project.getService(EasyCoderSideWindowService.class)
                    .notifyIdeAppInstance(new Gson().toJson(webviewMessage));
        }

        return null;
    }
}