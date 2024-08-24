package com.easycoder.intellij.enums;

public enum CodeCompletionLengthShaun {
    AUTO("Auto"),
    ONE_LINE("One Line"),
    MULTI_LINE("Multi Line");

    private final String description;

    CodeCompletionLengthShaun(String description) {
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