package com.easycoder.intellij.services;

import com.easycoder.intellij.window.EasyCoderSideWindow;
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
    }


    public void notifyIdeAppInstance(@NotNull JsonObject result) {
        CefBrowser browser = this.getEasyCoderSideWindow().jbCefBrowser().getCefBrowser();
        browser.executeJavaScript("window.postMessage(" + result + ",'*');", browser.getURL(), 0);
    }
}
