package com.easycoder.intellij.services;

import com.easycoder.intellij.handlers.GlobalStore;
import com.easycoder.intellij.settings.EasyCoderSettings;
import com.easycoder.intellij.utils.EasyCoderUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class EasyCoderCompleteService {

    public String[] getCodeCompletionHints(CharSequence editorContents, int cursorPosition) {
        EasyCoderSettings settings = EasyCoderSettings.getInstance();
        String contents = editorContents.toString();

        String prefix = contents.substring(EasyCoderUtils.prefixHandle(0, cursorPosition), cursorPosition);
        String suffix = contents.substring(cursorPosition, EasyCoderUtils.suffixHandle(cursorPosition, editorContents.length()));

        String generatedText = buildApiPostForBackend(settings, prefix, suffix);
        if (StringUtils.isBlank(generatedText)) {
            return null;
        }
        return new String[] { generatedText };
    }

    private String buildApiPostForBackend(EasyCoderSettings settings, String prefix, String suffix) {
        String token = PropertiesComponent.getInstance().getValue("easycoder:token");
        if (token == null) {
            return "";
        }
        GlobalStore.loading = true;


        String apiURL = "http://easycoder.puhuacloud.com/api/easycoder-api/app/session/completions";
        HttpPost httpPost = new HttpPost(apiURL);

        httpPost.setHeader("Content-Type", "application/json");

        JsonObject body = new JsonObject();
        body.addProperty("prefix", prefix);
        body.addProperty("suffix", suffix);
        body.addProperty("rows", 0);
        StringEntity requestEntity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);

        int timeoutMs = 10_000;
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(timeoutMs)
            .setSocketTimeout(timeoutMs)
            .build();
        httpPost.setConfig(requestConfig);

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try {
                    CloseableHttpClient httpClient = HttpClients.createDefault();

                    httpPost.setHeader("token", token);

                    HttpResponse response = httpClient.execute(httpPost);
                    String responseBody = EntityUtils.toString(response.getEntity());
                    httpClient.close();
                    
                    JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();
                    return jsonResponse.getAsJsonObject("data").get("content").getAsString();
                } catch (Exception e) {
                    return "";
                }
            }).orTimeout(timeoutMs, TimeUnit.MILLISECONDS)
            .exceptionally(e -> "");

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return "";
        } finally {
            GlobalStore.loading = false;
        }
    }

    public int getStatus() {
        return 200;
    }

}
