package com.easycoder.intellij.services;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.easycoder.intellij.handlers.GlobalStore;
import com.easycoder.intellij.settings.EasyCoderSettings;
import com.easycoder.intellij.utils.EasyCoderUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

public class EasyCoderCompleteService {

    private int requestStatus = 0;
    private Project project;

    public EasyCoderCompleteService(Project project) {
        this.project = project;
    }

    public String[] getCodeCompletionHints(CharSequence editorContents, int cursorPosition) {
        EasyCoderSettings settings = EasyCoderSettings.getInstance();
        String contents = editorContents.toString();

        String prefix = contents.substring(EasyCoderUtils.prefixHandle(0, cursorPosition), cursorPosition);
        String suffix = contents.substring(cursorPosition, EasyCoderUtils.suffixHandle(cursorPosition, editorContents.length()));

        String generatedText = buildApiPostForBackend(settings, prefix, suffix);
        if (StringUtils.isBlank(generatedText)) {
            voteStatus().noSuggestion();
            return null;
        }
        return new String[] { generatedText };
    }

    private String buildApiPostForBackend(EasyCoderSettings settings, String prefix, String suffix) {
        String token = PropertiesComponent.getInstance().getValue("easycoder:token");
        if (token == null) {
            return "";
        }
        voteStatus().loading();

        long startTime = System.currentTimeMillis();
        System.out.println("=== completion start === ");
        String serverAddress = EasyCoderSettings.getInstance().getServerAddressShaun();
        String apiURL = serverAddress + "/api/easycoder-api/app/session/completions";
        HttpPost httpPost = new HttpPost(apiURL);

        httpPost.setHeader("Content-Type", "application/json");

        JsonObject body = new JsonObject();
        body.addProperty("prefix", prefix);
        body.addProperty("suffix", suffix);
        body.addProperty("rows", settings.getCodeCompletionLengthShaun().getValue());
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
            String result = future.get();
            if (StringUtils.isBlank(result)) {
                voteStatus().noSuggestion();
            } else {
                voteStatus().done();
            }
            return result;
        } catch (InterruptedException | ExecutionException e) {
            voteStatus().noSuggestion();
            return "";
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("fetch ghost text duration: " + duration + " ms");
        }
    }

    public int getStatus() {
        return requestStatus;
    }

    private VoteStatus voteStatus() {
        return new VoteStatus();
    }

    private class VoteStatus {
        private static final int VOTE = 1;

        public void done() {
            requestStatus = Math.max(1, requestStatus) - VOTE;
            updateStatus();
        }

        public void loading() {
            requestStatus = Math.max(0, requestStatus) + VOTE;
            updateStatus();
        }

        public void noSuggestion() {
            requestStatus = -1;
            updateStatus();
        }

        private void updateStatus() {
            ApplicationManager.getApplication().invokeLater(() -> {
                if (requestStatus > 0) {
                    GlobalStore.status = "loading";
                    GlobalStore.text = "$(sync~spin) EasyCoder";
                    GlobalStore.tooltip = "Extension is running, please wait a moment";
                } else if (requestStatus == 0) {
                    GlobalStore.status = "done";
                    GlobalStore.text = "EasyCoder: Done";
                    GlobalStore.tooltip = "Extension completed";
                } else {
                    GlobalStore.status = "no-suggestion";
                    GlobalStore.text = "EasyCoder: No suggestion";
                    GlobalStore.tooltip = "No suggestion returned";
                }

                // TODO: Update the status bar with the new text and tooltip
                // This part depends on how you're managing the status bar in your IntelliJ plugin
                // You might need to create a method to update the status bar or use an existing one
                // For example:
                // updateStatusBar(GlobalStore.text, GlobalStore.tooltip);
            });
        }
    }
}
