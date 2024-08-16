package com.easycoder.intellij.http;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import com.easycoder.intellij.enums.MessageId;
import com.easycoder.intellij.model.WebviewMessage;
import com.easycoder.intellij.services.EasyCoderSideWindowService;
import com.google.gson.JsonObject;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.easycoder.intellij.constant.HttpRequest.baseURL;

public class HttpToolkits {
    static public String doHttpGet(WebviewMessage message) {
        String route = (String) message.getPayload().get("route");

        String token = PropertiesComponent.getInstance().getValue("easycoder:token");

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(baseURL + route))
            .GET();

        // 添加 headers
        requestBuilder.header("token", token);

        HttpRequest request = requestBuilder.build();

        System.out.println("[EasyCoder] -- HttpUtil.doHttpGet -- url: " + baseURL + route);

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[EasyCoder] -- HttpUtil.doHttpGet -- response: " + response.body());
            return response.body();
        } catch (IOException | InterruptedException e) {
            System.err.println("[Request Failed] -- " + e.getMessage());
            return null;
        }
    }

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

    /**
     * 1. 把消息传给 webview
     * 2. 全局记录 account 信息
     * @param project
     * @param map
     * @return
     */
    static public void signIn(Project project, Map<String, String> map) {
        JsonObject request = new JsonObject();
        request.addProperty("id", MessageId.SuccessfulAuth.name());

        JsonObject payload = new JsonObject();
        payload.addProperty("account", map.get("username"));
        payload.addProperty("accessToken", map.get("token"));

        request.add("payload", payload);

        PropertiesComponent.getInstance().setValue("easycoder:token", map.get("token"));
        PropertiesComponent.getInstance().setValue("easycoder:username", map.get("username"));
        PropertiesComponent.getInstance().setValue("easycoder:userId", map.get("userId"));

        project.getService(EasyCoderSideWindowService.class).notifyIdeAppInstance(request);
    }

    static public void signOut(Project project) {
        PropertiesComponent.getInstance().unsetValue("easycoder:token");
        PropertiesComponent.getInstance().unsetValue("easycoder:username");
        PropertiesComponent.getInstance().unsetValue("easycoder:userId");

        JsonObject message = new JsonObject();
        message.addProperty("id", MessageId.SignOutExtension.name());
        project.getService(EasyCoderSideWindowService.class).notifyIdeAppInstance(message);
    }
}
