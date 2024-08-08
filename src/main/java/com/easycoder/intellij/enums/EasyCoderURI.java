package com.easycoder.intellij.enums;

public enum EasyCoderURI {

    CPU_COMPLETE("/infill"),
    CPU_CHAT("/completion"),
    GPU_COMPLETE("/generate"),
    GPU_CHAT("/generate_stream");

    private final String uri;

    EasyCoderURI(String uri) {
        this.uri = uri;
    }

    public String getUri() {
        return uri;
    }

}
