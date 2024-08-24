package com.easycoder.intellij.enums;

public enum CodeCompletionDelayShaun {
    DELAY_500("500"),
    DELAY_1000("1000"),
    DELAY_1500("1500"),
    DELAY_2000("2000");

    private final String description;

    CodeCompletionDelayShaun(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}