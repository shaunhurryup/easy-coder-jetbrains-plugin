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

    public void needSignIn() {
        vote = -2;
        updateStatus();
    }

    public void serverError() {
        vote = -3;
        updateStatus();
    }

    private void updateStatus() {
        switch (vote) {
            case 0 -> {
                text = "Done";
                tooltip = "Extension completed";
            }
            case -1 -> {
                text = "No Suggestion";
                tooltip = "No suggestion returned";
            }
            case -2 -> {
                text = "Need Sign In";
                tooltip = "Please sign in to the extension first";
            }
            case -3 -> {
                text = "Server Error";
                tooltip = "Some error occurred in the server";
            }
            default -> {
                text = "Loading";
                tooltip = "Extension is running, please wait a moment";
            }
        }
    }
}