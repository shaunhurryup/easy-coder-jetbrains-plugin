package com.easycoder.intellij.services;

import com.easycoder.intellij.handlers.GlobalStore;
import com.easycoder.intellij.widget.DynamicStatusBarWidget;
import com.easycoder.intellij.widget.StatusPopup;
import com.easycoder.intellij.widget.StatusPopupFactory;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;

public class CodeCompletionProcess {
    private static final int STEP = 1;
    private int vote = 0;
    private String text = "";
    private String tooltip = "";

    public void done() {
        vote = Math.max(1, vote) - STEP;
        updateStatus();
    }

    public void loading() {
        vote = Math.max(0, vote) + STEP;
        updateStatus();
    }

    public void noSuggestion() {
        vote = -1;
        updateStatus();
    }

    public String getText() {
        return text;
    }

    public String getTooltip() {
        return tooltip;
    }

    private void updateStatus() {
        if (vote > 0) {
            text = "EasyCoder: Loading";
            tooltip = "Extension is running, please wait a moment";
        } else if (vote == 0) {
            text = "EasyCoder: Done";
            tooltip = "Extension completed";
        } else {
            text = "EasyCoder: No suggestion";
            tooltip = "No suggestion returned";
        }
    }
}