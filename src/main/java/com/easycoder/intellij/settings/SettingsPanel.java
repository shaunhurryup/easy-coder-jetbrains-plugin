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

    private JPanel modelRuntime;
    private JPanel Parameters;
    private JCheckBox enableSAYTCheckBox;
    private JPanel Settings;
    private JPanel ParamOuter;
    private JPanel TabActionPanel;
    private JComboBox<TabActionOption> tabActionComboBox;
    private JLabel tabActionLabel;
    private JComboBox<CompletionMaxToken> completionMaxTokensComboBox;
    private JComboBox<ChatMaxToken> chatMaxTokensComboBox;
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

        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(useGPUModelRadioButton);
        buttonGroup.add(useCPUModelRadioButton);

        tabActionComboBox.setModel(new EnumComboBoxModel<>(TabActionOption.class));
        enableSAYTCheckBox.addActionListener(e -> {
            tabActionLabel.setEnabled(enableSAYTCheckBox.isSelected());
            tabActionComboBox.setEnabled(enableSAYTCheckBox.isSelected());
        });

        completionMaxTokensComboBox.setModel(new EnumComboBoxModel<>(CompletionMaxToken.class));
        chatMaxTokensComboBox.setModel(new EnumComboBoxModel<>(ChatMaxToken.class));

        codeCompletionLengthShaun.setModel(new EnumComboBoxModel<>(CodeCompletionLengthShaun.class));
        codeCompletionDelayShaun.setModel(new EnumComboBoxModel<>(CodeCompletionDelayShaun.class));
        chatMaxTokensShaun.setModel(new EnumComboBoxModel<>(ChatMaxTokensShaun.class));
    }

    public JComponent getPanel() {
        return panel;
    }

    public boolean getCPUModelRadioButton() {
        return useCPUModelRadioButton.isSelected();
    }

    public void setCPUModelRadioButton(boolean enabledCPURadioButton) {
        useCPUModelRadioButton.setSelected(enabledCPURadioButton);
    }

    public boolean getGPUModelRadioButton() {
        return useGPUModelRadioButton.isSelected();
    }

    public void setGPUModelRadioButton(boolean enabledGPURadioButton) {
        useGPUModelRadioButton.setSelected(enabledGPURadioButton);
    }

    public String getServerAddressUrl() {
        return serverAddressTextField.getText();
    }

    public void setServerAddressUrl(String serverAddress) {
        serverAddressTextField.setText(serverAddress);
    }

    public boolean getEnableSAYTCheckBox() {
        return enableSAYTCheckBox.isSelected();
    }

    public void setEnableSAYTCheckBox(boolean enableSAYT) {
        enableSAYTCheckBox.setSelected(enableSAYT);
    }

    public TabActionOption getTabActionOption() {
        return (TabActionOption) tabActionComboBox.getModel().getSelectedItem();
    }

    public void setTabActionOption(TabActionOption option) {
        tabActionComboBox.getModel().setSelectedItem(option);
    }

    public CompletionMaxToken getCompletionMaxTokens() {
        return (CompletionMaxToken) completionMaxTokensComboBox.getModel().getSelectedItem();
    }

    public void setCompletionMaxTokens(CompletionMaxToken option) {
        completionMaxTokensComboBox.getModel().setSelectedItem(option);
    }

    public ChatMaxToken getChatMaxTokens() {
        return (ChatMaxToken) chatMaxTokensComboBox.getModel().getSelectedItem();
    }

    public void setChatMaxTokens(ChatMaxToken option) {
        chatMaxTokensComboBox.getModel().setSelectedItem(option);
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
