package com.easycoder.intellij.handlers;

import com.easycoder.intellij.constant.HttpRequest;
import com.easycoder.intellij.enums.MessageId;
import com.easycoder.intellij.http.HttpToolkits;
import com.easycoder.intellij.model.WebviewMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.PropertiesComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public class WebviewMessageHandler {
    public static WebviewMessage run(WebviewMessage message) {
        MessageId messageId = message.getId();

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

                HashMap<String, Object> payload = new HashMap<>();
                payload.put("account", data.get("username"));
                payload.put("accessToken", data.get("token"));

                return WebviewMessage.builder()
                    .id(MessageId.SuccessfulAuth)
                    .payload(payload)
                    .build();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (messageId.equals(MessageId.WebviewInitQaExamples)) {
            String modifiedStream = HttpToolkits.doHttpGet(message);
            HashMap<String, Object> payload = new HashMap<>();
            // 直接传 string 嵌套的 json 不会被 webview 端解析
            // 如果要保留完整的 json 需要转换
            payload.put("$response", new Gson().fromJson(modifiedStream, JsonObject.class));
            return WebviewMessage.builder()
                .id(messageId)
                .payload(payload)
                .build();
        }

        if (messageId.equals(MessageId.WebviewMount)) {
            String token = PropertiesComponent.getInstance().getValue("easycoder:token");
            String username = PropertiesComponent.getInstance().getValue("easycoder:username");
            HashMap<String, Object> payload = new HashMap<>();
            payload.put("account", username);
            payload.put("accessToken", token);

            return WebviewMessage.builder()
                .id(MessageId.SuccessfulAuth)
                .payload(payload)
                .build();

        }

        return null;
    }
}