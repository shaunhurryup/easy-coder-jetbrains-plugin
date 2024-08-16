package com.easycoder.intellij.handlers;

import com.easycoder.intellij.constant.HttpRequest;
import com.easycoder.intellij.enums.MessageId;
import com.easycoder.intellij.http.HttpToolkits;
import com.easycoder.intellij.model.WebviewMessage;
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

        return null;
    }
}