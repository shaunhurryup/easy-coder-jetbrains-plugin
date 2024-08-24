package com.easycoder.intellij.settings;

import com.easycoder.intellij.enums.ChatMaxToken;
import com.easycoder.intellij.enums.ChatMaxTokensShaun;
import com.easycoder.intellij.enums.CodeCompletionDelayShaun;
import com.easycoder.intellij.enums.CodeCompletionLengthShaun;
import com.easycoder.intellij.enums.CompletionMaxToken;
import com.easycoder.intellij.enums.TabActionOption;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@State(name = "EasyCoderSettings", storages = @Storage("easycoder_settings.xml"))
public class EasyCoderSettings implements PersistentStateComponent<Element> {
    public static final String SETTINGS_TAG = "EasyCoderSettings";
    private static final String SERVER_ADDRESS_TAG = "SERVER_ADDRESS_URL";
    private static final String SAYT_TAG = "SAYT_ENABLED";
    private static final String CPU_RADIO_BUTTON_TAG = "CPU_RADIO_BUTTON_ENABLED";
    private static final String GPU_RADIO_BUTTON_TAG = "GPU_RADIO_BUTTON_ENABLED";
    private static final String TAB_ACTION_TAG = "TAB_ACTION";
    private static final String COMPLETION_MAX_TOKENS_TAG = "COMPLETION_MAX_TOKENS";
    private static final String CHAT_MAX_TOKENS_TAG = "CHAT_MAX_TOKENS";
    private static final String SERVER_ADDRESS_SHAUN_TAG = "SERVER_ADDRESS_SHAUN";
    private static final String ENABLE_CODE_COMPLETION_TAG = "ENABLE_CODE_COMPLETION";
    private static final String CODE_COMPLETION_LENGTH_TAG = "CODE_COMPLETION_LENGTH";
    private static final String CODE_COMPLETION_DELAY_TAG = "CODE_COMPLETION_DELAY";
    private static final String CHAT_MAX_TOKENS_DROPDOWN_TAG = "CHAT_MAX_TOKENS_DROPDOWN";
    private boolean saytEnabled = true;
    private boolean cpuRadioButtonEnabled = true;
    private boolean gpuRadioButtonEnabled = false;
    private String serverAddressURL = "http://127.0.0.1:8080";
    private TabActionOption tabActionOption = TabActionOption.ALL;
    private CompletionMaxToken completionMaxToken = CompletionMaxToken.MEDIUM;
    private ChatMaxToken chatMaxToken = ChatMaxToken.MEDIUM;
    private String serverAddressShaun = "";
    private boolean enableCodeCompletionShaun = true;
    private CodeCompletionLengthShaun codeCompletionLengthShaun = CodeCompletionLengthShaun.AUTO;
    private CodeCompletionDelayShaun codeCompletionDelayShaun = CodeCompletionDelayShaun.DELAY_500;
    private ChatMaxTokensShaun chatMaxTokensShaun = ChatMaxTokensShaun.TOKEN_1024;

    private static final EasyCoderSettings SHELL_CODER_SETTINGS_INSTANCE = new EasyCoderSettings();

    @Override
    public @Nullable Element getState() {
        Element state = new Element(SETTINGS_TAG);
        state.setAttribute(CPU_RADIO_BUTTON_TAG, Boolean.toString(isCPURadioButtonEnabled()));
        state.setAttribute(GPU_RADIO_BUTTON_TAG, Boolean.toString(isGPURadioButtonEnabled()));
        state.setAttribute(SERVER_ADDRESS_TAG, getServerAddressURL());
        state.setAttribute(SAYT_TAG, Boolean.toString(isSaytEnabled()));
        state.setAttribute(TAB_ACTION_TAG, getTabActionOption().name());
        state.setAttribute(COMPLETION_MAX_TOKENS_TAG, getCompletionMaxToken().name());
        state.setAttribute(CHAT_MAX_TOKENS_TAG, getChatMaxToken().name());
        state.setAttribute(SERVER_ADDRESS_SHAUN_TAG, getServerAddressShaun());
        state.setAttribute(ENABLE_CODE_COMPLETION_TAG, Boolean.toString(isEnableCodeCompletionShaun()));
        state.setAttribute(CODE_COMPLETION_LENGTH_TAG, getCodeCompletionLengthShaun().name());
        state.setAttribute(CODE_COMPLETION_DELAY_TAG, getCodeCompletionDelayShaun().name());
        state.setAttribute(CHAT_MAX_TOKENS_DROPDOWN_TAG, getChatMaxTokensShaun().name());
        return state;
    }

    @Override
    public void loadState(@NotNull Element state) {
        if (Objects.nonNull(state.getAttributeValue(CPU_RADIO_BUTTON_TAG))) {
            setCPURadioButtonEnabled(Boolean.parseBoolean(state.getAttributeValue(CPU_RADIO_BUTTON_TAG)));
        }
        if (Objects.nonNull(state.getAttributeValue(GPU_RADIO_BUTTON_TAG))) {
            setGPURadioButtonEnabled(Boolean.parseBoolean(state.getAttributeValue(GPU_RADIO_BUTTON_TAG)));
        }
        if (Objects.nonNull(state.getAttributeValue(SERVER_ADDRESS_TAG))) {
            setServerAddressURL(state.getAttributeValue(SERVER_ADDRESS_TAG));
        }
        if (Objects.nonNull(state.getAttributeValue(SAYT_TAG))) {
            setSaytEnabled(Boolean.parseBoolean(state.getAttributeValue(SAYT_TAG)));
        }
        if (Objects.nonNull(state.getAttributeValue(TAB_ACTION_TAG))) {
            setTabActionOption(TabActionOption.valueOf(state.getAttributeValue(TAB_ACTION_TAG)));
        }
        if (Objects.nonNull(state.getAttributeValue(COMPLETION_MAX_TOKENS_TAG))) {
            setCompletionMaxToken(CompletionMaxToken.valueOf(state.getAttributeValue(COMPLETION_MAX_TOKENS_TAG)));
        }
        if (Objects.nonNull(state.getAttributeValue(CHAT_MAX_TOKENS_TAG))) {
            setChatMaxToken(ChatMaxToken.valueOf(state.getAttributeValue(CHAT_MAX_TOKENS_TAG)));
        }
        if (Objects.nonNull(state.getAttributeValue(SERVER_ADDRESS_SHAUN_TAG))) {
            setServerAddressShaun(state.getAttributeValue(SERVER_ADDRESS_SHAUN_TAG));
        }
        if (Objects.nonNull(state.getAttributeValue(ENABLE_CODE_COMPLETION_TAG))) {
            setEnableCodeCompletionShaun(Boolean.parseBoolean(state.getAttributeValue(ENABLE_CODE_COMPLETION_TAG)));
        }
        if (Objects.nonNull(state.getAttributeValue(CODE_COMPLETION_LENGTH_TAG))) {
            setCodeCompletionLengthShaun(CodeCompletionLengthShaun.valueOf(state.getAttributeValue(CODE_COMPLETION_LENGTH_TAG)));
        }
        if (Objects.nonNull(state.getAttributeValue(CODE_COMPLETION_DELAY_TAG))) {
            setCodeCompletionDelayShaun(CodeCompletionDelayShaun.valueOf(state.getAttributeValue(CODE_COMPLETION_DELAY_TAG)));
        }
        if (Objects.nonNull(state.getAttributeValue(CHAT_MAX_TOKENS_DROPDOWN_TAG))) {
            setChatMaxTokensShaun(ChatMaxTokensShaun.valueOf(state.getAttributeValue(CHAT_MAX_TOKENS_DROPDOWN_TAG)  ));
        }
    }

    public static EasyCoderSettings getInstance() {
        if (Objects.isNull(ApplicationManager.getApplication())) {
            return SHELL_CODER_SETTINGS_INSTANCE;
        }

        EasyCoderSettings service = ApplicationManager.getApplication().getService(EasyCoderSettings.class);
        if (Objects.isNull(service)) {
            return SHELL_CODER_SETTINGS_INSTANCE;
        }
        return service;
    }

    public boolean isSaytEnabled() {
        return saytEnabled;
    }

    public void setSaytEnabled(boolean saytEnabled) {
        this.saytEnabled = saytEnabled;
    }

    public void toggleSaytEnabled() {
        this.saytEnabled = !this.saytEnabled;
    }

    public boolean isCPURadioButtonEnabled() {
        return cpuRadioButtonEnabled;
    }

    public void setCPURadioButtonEnabled(boolean cpuRadioButtonEnabled) {
        this.cpuRadioButtonEnabled = cpuRadioButtonEnabled;
    }

    public boolean isGPURadioButtonEnabled() {
        return gpuRadioButtonEnabled;
    }

    public void setGPURadioButtonEnabled(boolean gpuRadioButtonEnabled) {
        this.gpuRadioButtonEnabled = gpuRadioButtonEnabled;
    }

    public String getServerAddressURL() {
        return serverAddressURL;
    }

    public void setServerAddressURL(String serverAddressURL) {
        this.serverAddressURL = serverAddressURL;
    }

    public CompletionMaxToken getCompletionMaxToken() {
        return completionMaxToken;
    }

    public void setCompletionMaxToken(CompletionMaxToken completionMaxToken) {
        this.completionMaxToken = completionMaxToken;
    }

    public ChatMaxToken getChatMaxToken() {
        return chatMaxToken;
    }

    public void setChatMaxToken(ChatMaxToken chatMaxToken) {
        this.chatMaxToken = chatMaxToken;
    }

    public TabActionOption getTabActionOption() {
        return tabActionOption;
    }

    public void setTabActionOption(TabActionOption tabActionOption) {
        this.tabActionOption = tabActionOption;
    }

    public String getServerAddressShaun() {
        return serverAddressShaun;
    }

    public void setServerAddressShaun(String serverAddressShaun) {
        this.serverAddressShaun = serverAddressShaun;
    }

    public boolean isEnableCodeCompletionShaun() {
        return enableCodeCompletionShaun;
    }

    public void setEnableCodeCompletionShaun(boolean enableCodeCompletionShaun) {
        this.enableCodeCompletionShaun = enableCodeCompletionShaun;
    }

    public CodeCompletionLengthShaun getCodeCompletionLengthShaun() {
        return codeCompletionLengthShaun;
    }

    public void setCodeCompletionLengthShaun(CodeCompletionLengthShaun codeCompletionLengthShaun) {
        this.codeCompletionLengthShaun = codeCompletionLengthShaun;
    }

    public CodeCompletionDelayShaun getCodeCompletionDelayShaun() {
        return codeCompletionDelayShaun;
    }

    public void setCodeCompletionDelayShaun(CodeCompletionDelayShaun codeCompletionDelayShaun) {
        this.codeCompletionDelayShaun = codeCompletionDelayShaun;
    }

    public ChatMaxTokensShaun getChatMaxTokensShaun() {
        return chatMaxTokensShaun;
    }

    public void setChatMaxTokensShaun(ChatMaxTokensShaun chatMaxTokensShaun) {
        this.chatMaxTokensShaun = chatMaxTokensShaun;
    }
}
