package com.easycoder.intellij.model;

import com.easycoder.intellij.enums.MessageId;
import com.easycoder.intellij.enums.MessageType;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;


@Data
@Builder
public class WebviewMessage {
    MessageId id;
    MessageType type;
    HashMap<String, Object> payload;
}
