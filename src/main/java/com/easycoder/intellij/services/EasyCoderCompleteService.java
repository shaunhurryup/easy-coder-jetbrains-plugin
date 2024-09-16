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

import com.easycoder.intellij.constant.Const;
import com.easycoder.intellij.handlers.GlobalStore;
import com.easycoder.intellij.http.HttpToolkits;
import com.easycoder.intellij.settings.EasyCoderSettings;
import com.easycoder.intellij.utils.EasyCoderUtils;
import com.easycoder.intellij.widget.DynamicStatusBarWidget;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;

public class EasyCoderCompleteService {

    private CodeCompletionProcess codeCompletionProcess;

    public EasyCoderCompleteService() {
        codeCompletionProcess = new CodeCompletionProcess();
    }

    public String[] getCodeCompletionHints(CharSequence editorContents, int cursorPosition, Project project) {
        DynamicStatusBarWidget widget = (DynamicStatusBarWidget) WindowManager.getInstance()
                .getStatusBar(project)
                .getWidget("DynamicStatusBarWidget");

        EasyCoderSettings settings = EasyCoderSettings.getInstance();
        String contents = editorContents.toString();

        String prefix = contents.substring(EasyCoderUtils.prefixHandle(0, cursorPosition), cursorPosition);
        String suffix = contents.substring(cursorPosition,
                EasyCoderUtils.suffixHandle(cursorPosition, editorContents.length()));

        String generatedText = buildApiPostForBackend(settings, prefix, suffix, widget);
        if (StringUtils.isBlank(generatedText)) {
            codeCompletionProcess.noSuggestion();
            widget.updateWidget(codeCompletionProcess.getText(), codeCompletionProcess.getTooltip());
            return new String[] {};
        }
        return new String[] { generatedText };
    }

    private String buildApiPostForBackend(
            EasyCoderSettings settings,
            String prefix,
            String suffix,
            DynamicStatusBarWidget widget) {
        String token = PropertiesComponent.getInstance().getValue("easycoder:token");
        if (token == null) {
            return "";
        }
        codeCompletionProcess.loading();
        widget.updateWidget(codeCompletionProcess.getText(), codeCompletionProcess.getTooltip());

        long startTime = System.currentTimeMillis();
        String serverAddress = EasyCoderSettings.getInstance().getServerAddressShaun();
        String apiURL = serverAddress + "/api/easycoder-api/app/session/completions";
        HttpPost httpPost = new HttpPost(apiURL);

        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("ide", HttpToolkits.getIdeProductName());

        JsonObject body = new JsonObject();
        body.addProperty("prefix", prefix);
        body.addProperty("suffix", suffix);
        body.addProperty("rows", settings.getCodeCompletionLengthShaun().getValue());
        StringEntity requestEntity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);

        int timeoutMs = Const.CODE_COMPLETION_TIMEOUT;
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
                codeCompletionProcess.noSuggestion();
                widget.updateWidget(codeCompletionProcess.getText(), codeCompletionProcess.getTooltip());
            } else {
                codeCompletionProcess.done();
                widget.updateWidget(codeCompletionProcess.getText(), codeCompletionProcess.getTooltip());
            }
            return result;
        } catch (InterruptedException | ExecutionException e) {
            codeCompletionProcess.noSuggestion();
            widget.updateWidget(codeCompletionProcess.getText(), codeCompletionProcess.getTooltip());
            return "";
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("fetch ghost text duration: " + duration + " ms");
        }
    }

}
