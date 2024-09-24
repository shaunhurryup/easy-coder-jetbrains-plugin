package com.easycoder.intellij.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CompletionResult {
    private String generatedText;
    private String recordId;
}