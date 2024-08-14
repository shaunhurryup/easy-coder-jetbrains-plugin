package com.easycoder.intellij.window;

import com.easycoder.intellij.enums.EasyCoderURI;
import com.easycoder.intellij.handlers.CustomSchemeHandlerFactory;
import com.easycoder.intellij.services.EasyCoderSideWindowService;
import com.easycoder.intellij.settings.EasyCoderSettings;
import com.easycoder.intellij.utils.EasyCoderUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefBrowserBase;
import com.intellij.ui.jcef.JBCefJSQuery;
import org.cef.CefApp;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLifeSpanHandlerAdapter;
import org.cef.handler.CefLoadHandler;
import org.cef.network.CefRequest;

import javax.swing.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class EasyCoderSideWindow {

    private final Logger logger = Logger.getInstance(this.getClass());
    private JBCefBrowser jbCefBrowser;
    private final Project project;
    private boolean webLoaded;

    public EasyCoderSideWindow(Project project) {
        super();
        this.webLoaded = false;
        this.project = project;
    }

    public synchronized JBCefBrowser jbCefBrowser() {
        return !this.webLoaded ? lazyLoad() : this.jbCefBrowser;
    }

    private JBCefBrowser lazyLoad() {
        try {
            String ideaVersion = EasyCoderUtils.getIDEVersion("major");
            if (!this.webLoaded) {
                boolean isOffScreenRendering = true;
                if (SystemInfo.isMac) {
                    isOffScreenRendering = false;
                } else if (SystemInfo.isLinux || SystemInfo.isUnix) {
                    int version = Integer.parseInt(ideaVersion);
                    if (version >= 2023) {
                        isOffScreenRendering = true;
                    } else if (version < 2023) {
                        isOffScreenRendering = false;
                    }
                } else if (SystemInfo.isWindows) {
                    isOffScreenRendering = false;
                }
                JBCefBrowser browser;
                try {
                    browser = JBCefBrowser.createBuilder().setOffScreenRendering(isOffScreenRendering).build();
                } catch (Exception e) {
                    logger.error("JBCefBrowser# build Browser not supported", e);
                    browser = new JBCefBrowser();
                }

                registerLifeSpanHandler(browser);
                registerJsCallJavaHandler(browser);
                browser.loadURL("http://easycoder/index.html?");
                this.jbCefBrowser = browser;
                this.webLoaded = true;
            }
        } catch (Exception e) {
            logger.error("JBCefBrowser lazyLoad error", e);
        }
        return this.jbCefBrowser;
    }

    private String mockHttpGet() {
        try {
            // 创建 HttpClient 对象
            HttpClient client = HttpClient.newHttpClient();

            // 创建 HttpRequest 对象
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://jsonplaceholder.typicode.com/todos/1"))
                    .GET()
                    .build();

            // 发送请求并获取响应
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void registerJsCallJavaHandler(JBCefBrowser browser) {
        JBCefJSQuery query = JBCefJSQuery.create((JBCefBrowserBase) browser);
        query.addHandler((String arg) -> {
            try {
                Object request = new Gson().fromJson(arg, Object.class);
                String mockHttpGetResponse = mockHttpGet();
                return new JBCefJSQuery.Response(mockHttpGetResponse);
            } catch (Exception e) {
                logger.warn("JBCefJSQuery error", e);
                return new JBCefJSQuery.Response(null, 0, "errorMsg");
            }
        });
        browser.getJBCefClient().addLoadHandler(new CefLoadHandler() {
            @Override
            public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {
            }

            @Override
            public void onLoadStart(CefBrowser browser, CefFrame frame, CefRequest.TransitionType transitionType) {
                browser.executeJavaScript(
                        // Webview => JBCef
                        // arg 如果是对象需要序列化
                        "window.callJava = function(arg) {" +
                                query.inject(
                                        "arg",
                                        // onSuccess
                                        "response => console.log('response: ', response)",
                                        // onError
                                        "(error_code, error_message) => console.log('callJava 失败', error_code, error_message)"
                                ) +
                                "};",
                        null, 0);
            }

            @Override
            public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
                JsonObject jsonObject = new JsonObject();
                if (EasyCoderSettings.getInstance().isCPURadioButtonEnabled()) {
                    jsonObject.addProperty("sendUrl", EasyCoderSettings.getInstance().getServerAddressURL() + EasyCoderURI.CPU_CHAT.getUri());
                    jsonObject.addProperty("modelType", "CPU");
                } else {
                    jsonObject.addProperty("sendUrl", EasyCoderSettings.getInstance().getServerAddressURL() + EasyCoderURI.GPU_CHAT.getUri());
                    jsonObject.addProperty("modelType", "GPU");
                }
                jsonObject.addProperty("maxToken", EasyCoderSettings.getInstance().getChatMaxToken().getDescription());
                JsonObject result = new JsonObject();
                result.addProperty("data", jsonObject.toString());
                (project.getService(EasyCoderSideWindowService.class)).notifyIdeAppInstance(result);
            }

            @Override
            public void onLoadError(CefBrowser browser, CefFrame frame, ErrorCode errorCode, String errorText, String failedUrl) {
                logger.error("JBCefBrowser# onLoadError, failedUrl:{}, errorText:{}", failedUrl, errorText);
            }
        }, browser.getCefBrowser());
    }

    private void registerLifeSpanHandler(JBCefBrowser browser) {
        final CefLifeSpanHandlerAdapter lifeSpanHandlerAdapter = new CefLifeSpanHandlerAdapter() {
            @Override
            public void onAfterCreated(CefBrowser browse) {
                CefApp.getInstance().registerSchemeHandlerFactory("http", "easycoder", new CustomSchemeHandlerFactory(EasyCoderSideWindow.this.project));
            }
        };
        browser.getJBCefClient().addLifeSpanHandler(lifeSpanHandlerAdapter, browser.getCefBrowser());
        final JBCefBrowser tempBrowser = browser;
        Disposer.register(this.project, () -> tempBrowser.getJBCefClient().removeLifeSpanHandler(lifeSpanHandlerAdapter, tempBrowser.getCefBrowser()));
    }

    public JComponent getComponent() {
        if (Objects.nonNull(jbCefBrowser())) {
            return jbCefBrowser().getComponent();
        }
        return null;
    }


}





