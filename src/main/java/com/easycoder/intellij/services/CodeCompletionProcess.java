package com.easycoder.intellij.services;

import lombok.Getter;

public class CodeCompletionProcess {
    private static final int STEP = 1;
    private int vote = 0;
    @Getter
    private String text = "";
    @Getter
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

    private void updateStatus() {
        if (vote > 0) {
            text = "Loading";
            tooltip = "Extension is running, please wait a moment";
        } else if (vote == 0) {
            text = "Done";
            tooltip = "Extension completed";
        } else {
            text = "No suggestion";
            tooltip = "No suggestion returned";
        }
    }
}