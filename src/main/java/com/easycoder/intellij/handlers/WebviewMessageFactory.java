package com.easycoder.intellij.handlers;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import com.easycoder.intellij.enums.MessageId;
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
                CompletableFuture<Map<String, Object>> pollingFuture = CompletableFuture.supplyAsync(() -> {
                    String serverUrl = "http://easycoder.puhuacloud.com/api/easycoder-api/app/user/getToken/" + uuid;
                    int rest = 60;
                    while (rest-- > 0) {
                        try {
                            String response = HttpUtil.get(serverUrl);
                            JSONObject jsonResponse = new JSONObject(response);
                            if (jsonResponse.getInt("code") == 200) {
                                JSONObject data = jsonResponse.getJSONObject("data");
                                String username = data.getStr("username");
                                String userId = data.getStr("userId");
                                String token = data.getStr("token");
                                if (username == null || userId == null) {
                                    throw new RuntimeException("Fail to sign in, missing username or userId");
                                }
                                // TODO: Implement Station.flow().p2w() equivalent
                                return Map.of("token", token, "username", username, "userId", userId);
                            }
                        } catch (Exception e) {
                            System.err.println("[EasyCoder] -- HttpUtil.polling -- Fail to request server " + serverUrl + ": " + e.getMessage());
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                    return null;
                });
                Map<String, Object> data = pollingFuture.get(); // This will block until the polling is done

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