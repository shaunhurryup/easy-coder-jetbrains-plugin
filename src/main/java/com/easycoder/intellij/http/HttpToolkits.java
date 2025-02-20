package com.easycoder.intellij.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.easycoder.intellij.enums.MessageId;
import com.easycoder.intellij.enums.MessageType;
import com.easycoder.intellij.enums.ServiceRoute;
import com.easycoder.intellij.model.WebviewMessage;
import com.easycoder.intellij.services.EasyCoderSideWindowService;
import com.easycoder.intellij.settings.EasyCoderSettings;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.project.Project;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;

public class HttpToolkits {
  public static String getBaseURL() {
    return EasyCoderSettings.getInstance().getServerAddressShaun();
  }

  public static String getIdeProductName() {
    // versionName show only product name
    // fullApplicationName show product name + version
    return ApplicationInfo.getInstance().getVersionName();
  }

  public static Runnable createEventSource(WebviewMessage message, Project project) {
    String route = message.getPayload().get("route").getAsString();
    Object body = message.getPayload().get("body");
    String method = message.getPayload().get("method") != null ? message.getPayload().get("method").getAsString()
        : "POST";

    String token = PropertiesComponent.getInstance().getValue("easycoder:token");
    String url = getBaseURL() + route;

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("token", token)
        .header("ide", getIdeProductName());

    // Add new field for abort functionality
    CompletableFuture<Void> abortFuture = new CompletableFuture<>();

    if ("POST".equalsIgnoreCase(method)) {
      requestBuilder.POST(HttpRequest.BodyPublishers.ofString(new Gson().toJson(body)))
          .header("Content-Type", "application/json");
    } else {
      requestBuilder.GET();
    }

    HttpRequest request = requestBuilder.build();

    try {
      notifyWebview(project, message.getId(), MessageType.HandleEventSourceStart, new JsonObject());

      HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
      String sessionId = response.headers().firstValue("sessionid").orElse("");
      String recordId = response.headers().firstValue("recordid").orElse("");

      if ((route.equals(ServiceRoute.SEND_QUESTION.getRoute())
          || route.equals(ServiceRoute.RE_GENERATE.getRoute()))
          && (sessionId.isEmpty() || recordId.isEmpty())) {
        throw new RuntimeException("Empty session-id or record-id");
      }

      // Modify the reading logic to include abort check
      new Thread(() -> {
        try (InputStream inputStream = response.body()) {
          byte[] buffer = new byte[1024];
          int bytesRead;

          while ((bytesRead = inputStream.read(buffer)) != -1) {
            // Check if abort was requested
            if (abortFuture.isDone()) {
              notifyWebview(
                  project,
                  message.getId(),
                  MessageType.HandleEventSourceAbort,
                  new JsonObject());
              return;
            }

            String chunk = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
            JsonObject payload = new JsonObject();
            payload.addProperty("data", chunk);
            payload.addProperty("recordId", recordId);
            payload.addProperty("sessionId", sessionId);
            notifyWebview(project, message.getId(), MessageType.HandleEventSourceMessage, payload);
          }

          notifyWebview(project, message.getId(), MessageType.HandleEventSourceSuccess, new JsonObject());
        } catch (Exception e) {
          JsonObject errorPayload = new JsonObject();
          errorPayload.addProperty("error", e.getMessage());
          notifyWebview(project, message.getId(), MessageType.HandleEventSourceError, errorPayload);
        }
      }).start();

      // Return a Runnable for the abort function
      return () -> abortFuture.complete(null);
    } catch (Exception e) {
      JsonObject errorPayload = new JsonObject();
      errorPayload.addProperty("error", e.getMessage());
      notifyWebview(project, message.getId(), MessageType.HandleEventSourceError, errorPayload);
    }

    // Return a no-op Runnable if an exception occurred
    return () -> {
    };
  }

  private static void notifyWebview(Project project, MessageId messageId, MessageType type, JsonObject payload) {
    WebviewMessage responseMessage = WebviewMessage.builder()
        .id(messageId)
        .type(type)
        .payload(payload)
        .build();
    project.getService(EasyCoderSideWindowService.class)
        .notifyIdeAppInstance(new com.google.gson.Gson().toJson(responseMessage));
  }

  static public String doHttpGet(WebviewMessage message) {
    String route = message.getPayload().get("route").getAsString();

    String token = PropertiesComponent.getInstance().getValue("easycoder:token");

    if (token == null) {
      return null;
    }

    HttpClient client = HttpClient.newHttpClient();

    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
        .uri(URI.create(getBaseURL() + route))
        .GET();

    // 添加 headers
    requestBuilder.header("token", token)
        .header("ide", getIdeProductName());

    HttpRequest request = requestBuilder.build();

    System.out.println("[EasyCoder] -- HttpUtil.doHttpGet -- url: " + getBaseURL() + route);

    try {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      String body = response.body();
      System.out.println("[EasyCoder] -- HttpUtil.doHttpGet -- response: " + body);
      return body;
    } catch (IOException | InterruptedException e) {
      System.err.println("[Request Failed] -- " + e.getMessage());
      return null;
    }
  }

  public static String doHttpPost(WebviewMessage message) {
    JsonObject payload = message.getPayload();
    String route = payload.get("route").getAsString();
    JsonObject body = payload.get("body").getAsJsonObject();

    String token = PropertiesComponent.getInstance().getValue("easycoder:token");

    if (token == null) {
      return null;
    }

    HttpClient client = HttpClient.newHttpClient();

    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
        .uri(URI.create(getBaseURL() + route))
        .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
        .header("Content-Type", "application/json")
        .header("token", token)
        .header("ide", getIdeProductName());

    HttpRequest request = requestBuilder.build();

    System.out.println("[EasyCoder] -- HttpUtil.doHttpPost -- url: " + getBaseURL() + route);
    System.out.println("[EasyCoder] -- HttpUtil.doHttpPost -- body: " + body);

    try {
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      String responseBody = response.body();
      System.out.println("[EasyCoder] -- HttpUtil.doHttpPost -- response: " + responseBody);
      return responseBody;
    } catch (IOException | InterruptedException e) {
      System.err.println("[Request Failed] -- " + e.getMessage());
      // VscodeMessage.error(e.getMessage());
      return null;
    }
  }

  static public CompletableFuture<Map<String, String>> fetchToken(String uuid) {
    return CompletableFuture.supplyAsync(() -> {
      String serverAddress = EasyCoderSettings.getInstance().getServerAddressShaun();
      String serverUrl = serverAddress + "/api/easycoder-api/app/user/getToken/" + uuid;
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
          System.err.println("[EasyCoder] -- HttpUtil.polling -- Fail to request server " + serverUrl + ": "
              + e.getMessage());
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
   * 
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