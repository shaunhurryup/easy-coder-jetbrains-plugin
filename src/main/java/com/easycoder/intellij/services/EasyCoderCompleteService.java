package com.easycoder.intellij.services;

import com.easycoder.intellij.constant.PrefixString;
import com.easycoder.intellij.enums.EasyCoderURI;
import com.easycoder.intellij.settings.EasyCoderSettings;
import com.easycoder.intellij.utils.EasyCoderUtils;
import com.easycoder.intellij.widget.EasyCoderWidget;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.WindowManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.regex.Pattern;

public class EasyCoderCompleteService {
    private final Logger logger = Logger.getInstance(this.getClass());
    private static final String PREFIX_TAG = "<fim_prefix>";
    private static final String SUFFIX_TAG = "<fim_suffix>";
    private static final String MIDDLE_TAG = "<fim_middle>";
    private static final String REG_EXP = "data:\\s?(.*?)\n";
    private static final Pattern PATTERN = Pattern.compile(REG_EXP);
    private static long lastRequestTime = 0;
    private static boolean httpRequestFinFlag = true;
    private int statusCode = 200;

    public String[] getCodeCompletionHints(CharSequence editorContents, int cursorPosition) {
        EasyCoderSettings settings = EasyCoderSettings.getInstance();
        String contents = editorContents.toString();
        if (!httpRequestFinFlag || !settings.isSaytEnabled() || StringUtils.isBlank(contents)) {
            return null;
        }
        if (contents.contains(PREFIX_TAG) || contents.contains(SUFFIX_TAG) || contents.contains(MIDDLE_TAG) || contents.contains(PrefixString.RESPONSE_END_TAG)) {
            return null;
        }
        String prefix = contents.substring(EasyCoderUtils.prefixHandle(0, cursorPosition), cursorPosition);
        String suffix = contents.substring(cursorPosition, EasyCoderUtils.suffixHandle(cursorPosition, editorContents.length()));
        String generatedText = "";
        String easyCoderPrompt = generateFIMPrompt(prefix, suffix);
        if(settings.isCPURadioButtonEnabled()){
            HttpPost httpPost = buildApiPostForCPU(settings, prefix, suffix);
            generatedText = getApiResponseForCPU(settings, httpPost, easyCoderPrompt);
        }else{
            HttpPost httpPost = buildApiPostForGPU(settings, easyCoderPrompt);
            generatedText = getApiResponseForGPU(settings, httpPost, easyCoderPrompt);
        }
        String[] suggestionList = null;
        if (generatedText.contains(MIDDLE_TAG)) {
            String[] parts = generatedText.split(MIDDLE_TAG);
            if (parts.length > 0) {
                suggestionList = StringUtils.splitPreserveAllTokens(parts[1], "\n");
                if (suggestionList.length == 1 && suggestionList[0].trim().isEmpty()) {
                    return null;
                }
                if (suggestionList.length > 1) {
                    for (int i = 0; i < suggestionList.length; i++) {
                        StringBuilder sb = new StringBuilder(suggestionList[i]);
                        sb.append("\n");
                        suggestionList[i] = sb.toString();
                    }
                }
            }
        }
        return suggestionList;
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
