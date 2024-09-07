package com.easycoder.intellij.enums;

public enum CodeCompletionLengthShaun {
    AUTO("Auto"),
    ONE_LINE("One-line"),
    MULTI_LINE("Multi-line");

    private final String description;

    CodeCompletionLengthShaun(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getValue() {
        return this.ordinal(); // Returns the ordinal value of the enum
    }

    @Override
    public String toString() {
        return description;
    }
}