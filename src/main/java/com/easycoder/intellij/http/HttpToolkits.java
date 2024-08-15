package com.easycoder.intellij.http;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HttpToolkits {
    static public CompletableFuture<Map<String, String>> fetchToken(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
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
    }
}
