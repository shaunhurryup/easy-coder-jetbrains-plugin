package com.easycoder.intellij.enums;

public enum ChatMaxTokensShaun {
    TOKEN_1024("1024"),
    TOKEN_2048("2048"),
    TOKEN_4096("4096"),
    TOKEN_8192("8192");

    private final String description;

    ChatMaxTokensShaun(String description) {
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