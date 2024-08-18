package com.easycoder.intellij.services;

import com.easycoder.intellij.constant.PrefixString;
import com.easycoder.intellij.enums.EasyCoderURI;
import com.easycoder.intellij.settings.EasyCoderSettings;
import com.easycoder.intellij.utils.EasyCoderUtils;
import com.easycoder.intellij.widget.EasyCoderWidget;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class EasyCoderCompleteService {
    private final Logger logger = Logger.getInstance(this.getClass());
    private static final String PREFIX_TAG = "<fim_prefix>";
    private static final String SUFFIX_TAG = "<fim_suffix>";
    private static final String MIDDLE_TAG = "<fim_middle>";
    private static final String REG_EXP = "data:\\s?(.*?)\n";
    private static final Pattern PATTERN = Pattern.compile(REG_EXP);
    private static boolean httpRequestFinFlag = true;
    private int statusCode = 200;

    public String[] getCodeCompletionHints(CharSequence editorContents, int cursorPosition) {
        EasyCoderSettings settings = EasyCoderSettings.getInstance();
        String contents = editorContents.toString();
        if (!httpRequestFinFlag || !settings.isSaytEnabled() || StringUtils.isBlank(contents)) {
            return null;
        }
        String prefix = contents.substring(EasyCoderUtils.prefixHandle(0, cursorPosition), cursorPosition);
        String suffix = contents.substring(cursorPosition, EasyCoderUtils.suffixHandle(cursorPosition, editorContents.length()));
        
        String generatedText = buildApiPostForBackend(settings, prefix, suffix);
        if (StringUtils.isBlank(generatedText)) {
            return null;
        }
        return new String[] { generatedText };
    }

    private String generateFIMPrompt(String prefix, String suffix) {
        return PREFIX_TAG + prefix + SUFFIX_TAG + suffix + MIDDLE_TAG;
    }

    private HttpPost buildApiPostForCPU(EasyCoderSettings settings, String prefix, String suffix) {
        String apiURL = settings.getServerAddressURL() + EasyCoderURI.CPU_COMPLETE.getUri();
        HttpPost httpPost = new HttpPost(apiURL);
        JsonObject httpBody = EasyCoderUtils.pakgHttpRequestBodyForCPU(settings, prefix, suffix);
        StringEntity requestEntity = new StringEntity(httpBody.toString(), ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);
        return httpPost;
    }

    private HttpPost buildApiPostForGPU(EasyCoderSettings settings, String easyCoderPrompt) {
        String apiURL = settings.getServerAddressURL() + EasyCoderURI.GPU_COMPLETE.getUri();
        HttpPost httpPost = new HttpPost(apiURL);
        JsonObject httpBody = EasyCoderUtils.pakgHttpRequestBodyForGPU(settings, easyCoderPrompt);
        StringEntity requestEntity = new StringEntity(httpBody.toString(), ContentType.APPLICATION_JSON);
        httpPost.setEntity(requestEntity);
        return httpPost;
    }

    private String buildApiPostForBackend(EasyCoderSettings settings, String prefix, String suffix) {
        httpRequestFinFlag = false;
        String apiURL = "http://easycoder.puhuacloud.com/api/easycoder-api/app/session/completions";
        HttpPost httpPost = new HttpPost(apiURL);

        httpPost.setHeader("Content-Type", "application/json");

        JsonObject body = new JsonObject();
        body.addProperty("prefix", prefix);
        body.addProperty("suffix", suffix);
        body.addProperty("rows", 3);
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

                    String token = PropertiesComponent.getInstance().getValue("easycoder:token");
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
            httpRequestFinFlag = true;
        }
    }

    private String getApiResponseForCPU(EasyCoderSettings settings, HttpPost httpPost, String easyCoderPrompt) {
        String responseText = "";
        httpRequestFinFlag = false;
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(httpPost);
            int oldStatusCode = statusCode;
            statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != oldStatusCode) {
                for (Project openProject : ProjectManager.getInstance().getOpenProjects()) {
                    WindowManager.getInstance().getStatusBar(openProject).updateWidget(EasyCoderWidget.ID);
                }
            }
            if (statusCode != 200) {
                return responseText;
            }
            String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
            responseText = EasyCoderUtils.parseHttpResponseContentForCPU(settings, responseBody, PATTERN);
            httpClient.close();
        } catch (IOException e) {
            logger.error("getApiResponseForCPU error", e);
        } finally {
            httpRequestFinFlag = true;
        }
        return easyCoderPrompt + responseText;
    }

    private String getApiResponseForGPU(EasyCoderSettings settings, HttpPost httpPost, String easyCoderPrompt) {
        String responseText = "";
        httpRequestFinFlag = false;
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(httpPost);
            int oldStatusCode = statusCode;
            statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != oldStatusCode) {
                for (Project openProject : ProjectManager.getInstance().getOpenProjects()) {
                    WindowManager.getInstance().getStatusBar(openProject).updateWidget(EasyCoderWidget.ID);
                }
            }
            if (statusCode != 200) {
                return responseText;
            }
            String responseBody = EntityUtils.toString(response.getEntity());
            responseText = EasyCoderUtils.parseHttpResponseContentForGPU(settings, responseBody);
            httpClient.close();
        } catch (IOException e) {
            logger.error("getApiResponseForGPU error", e);
        } finally {
            httpRequestFinFlag = true;
        }

        return easyCoderPrompt + responseText;
    }

    public int getStatus() {
        return statusCode;
    }


}
