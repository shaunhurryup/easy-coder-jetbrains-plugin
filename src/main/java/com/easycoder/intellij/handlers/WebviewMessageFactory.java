package com.easycoder.intellij.handlers;

import com.easycoder.intellij.enums.MessageId;
import com.easycoder.intellij.http.HttpToolkits;
import com.easycoder.intellij.model.WebviewMessage;
import com.intellij.ide.BrowserUtil;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;


public class WebviewMessageFactory {
    public static void handle(WebviewMessage message) {
        MessageId messageId = message.getId();

        if (messageId.equals(MessageId.OpenSignInWebpage)) {
            try {
                // 1) Create UUID
                String uuid = UUID.randomUUID().toString();

                // 2) Open sign-in page
                String targetUrl = "http://easycoder.puhuacloud.com?sessionId=" + uuid;
                BrowserUtil.browse(targetUrl);

                // 3) Polling
                CompletableFuture<Map<String, String>> pollingFuture = HttpToolkits.fetchToken(uuid);
                Map<String, String> data = pollingFuture.get(); // This will block until the polling is done

                // 4) Update accounts
                if (data == null) {
                    System.err.println("[EasyCoder]");
                    return;
                }

                // Execute VS Code command equivalent in Java
                // createSession(data.get("token").toString(), data.get("username").toString(), data.get("userId").toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}