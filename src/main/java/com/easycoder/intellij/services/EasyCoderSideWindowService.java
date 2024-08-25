package com.easycoder.intellij.services;

import com.easycoder.intellij.listener.ThemeListener;
import com.easycoder.intellij.model.WebviewMessage;
import com.easycoder.intellij.window.EasyCoderSideWindow;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.cef.browser.CefBrowser;
import org.jetbrains.annotations.NotNull;

@Service
public final class EasyCoderSideWindowService {

    private final Project project;
    private final EasyCoderSideWindow easyCoderSideWindow;

    public Project getProject() {
        return this.project;
    }

    public EasyCoderSideWindow getEasyCoderSideWindow() {
        return this.easyCoderSideWindow;
    }

    public EasyCoderSideWindowService(Project project) {
        this.project = project;
        this.easyCoderSideWindow = new EasyCoderSideWindow(project);
        ThemeListener.initialize();
    }


    public void notifyIdeAppInstance(@NotNull JsonObject result) {
        CefBrowser browser = this.getEasyCoderSideWindow().jbCefBrowser().getCefBrowser();
        browser.executeJavaScript("window.postMessage(" + result + ",'*');", browser.getURL(), 0);
    }
    public void notifyIdeAppInstance(@NotNull String result) {
        CefBrowser browser = this.getEasyCoderSideWindow().jbCefBrowser().getCefBrowser();
        browser.executeJavaScript("window.postMessage(" + result + ",'*');", browser.getURL(), 0);
    }

    public void notifyIdeAppInstance(@NotNull WebviewMessage message) {
        this.notifyIdeAppInstance(new Gson().toJson(message));
    }
}
