package com.easycoder.intellij.settings;

import com.easycoder.intellij.enums.ChatMaxToken;
import com.easycoder.intellij.enums.CompletionMaxToken;
import com.easycoder.intellij.enums.TabActionOption;
import com.easycoder.intellij.enums.CodeCompletionLengthShaun;
import com.easycoder.intellij.enums.CodeCompletionDelayShaun;
import com.easycoder.intellij.enums.ChatMaxTokensShaun;
import com.intellij.ui.EnumComboBoxModel;

import javax.swing.*;

public class SettingsPanel {

    private JPanel panel;
    private JTextField serverAddressTextField;

    private JRadioButton useGPUModelRadioButton;
    private JRadioButton useCPUModelRadioButton;
    private JPanel EasyCoderSettings;
    private JLabel CodeCompletionLength;

    // New fields with 'shaun' suffix
    private JTextField serverAddressShaun;
    private JCheckBox enableCodeCompletionShaun;
    private JComboBox<CodeCompletionLengthShaun> codeCompletionLengthShaun;
    private JComboBox<CodeCompletionDelayShaun> codeCompletionDelayShaun;
    private JComboBox<ChatMaxTokensShaun> chatMaxTokensShaun;

    public SettingsPanel() {
        codeCompletionLengthShaun.setModel(new EnumComboBoxModel<>(CodeCompletionLengthShaun.class));
        codeCompletionDelayShaun.setModel(new EnumComboBoxModel<>(CodeCompletionDelayShaun.class));
        chatMaxTokensShaun.setModel(new EnumComboBoxModel<>(ChatMaxTokensShaun.class));
    }

    public JComponent getPanel() {
        return panel;
    }

    // New getters and setters for the added fields with 'shaun' suffix
    public String getServerAddressShaun() {
        return serverAddressShaun.getText();
    }

    public void setServerAddressShaun(String address) {
        serverAddressShaun.setText(address);
    }

    public boolean getEnableCodeCompletionShaun() {
        return enableCodeCompletionShaun.isSelected();
    }

    public void setEnableCodeCompletionShaun(boolean enable) {
        enableCodeCompletionShaun.setSelected(enable);
    }

    public CodeCompletionLengthShaun getCodeCompletionLengthShaun() {
        return (CodeCompletionLengthShaun) codeCompletionLengthShaun.getSelectedItem();
    }

    public void setCodeCompletionLengthShaun(CodeCompletionLengthShaun length) {
        codeCompletionLengthShaun.setSelectedItem(length);
    }

    public CodeCompletionDelayShaun getCodeCompletionDelayShaun() {
        return (CodeCompletionDelayShaun) codeCompletionDelayShaun.getSelectedItem();
    }

    public void setCodeCompletionDelayShaun(CodeCompletionDelayShaun delay) {
        codeCompletionDelayShaun.setSelectedItem(delay);
    }

    public ChatMaxTokensShaun getChatMaxTokensShaun() {
        return (ChatMaxTokensShaun) chatMaxTokensShaun.getSelectedItem();
    }

    public void setChatMaxTokensShaun(ChatMaxTokensShaun tokens) {
        chatMaxTokensShaun.setSelectedItem(tokens);
    }
}
