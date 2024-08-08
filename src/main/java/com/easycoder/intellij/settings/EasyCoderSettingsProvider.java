package com.easycoder.intellij.settings;

import com.easycoder.intellij.enums.EasyCoderURI;
import com.easycoder.intellij.services.EasyCoderSideWindowService;
import com.easycoder.intellij.widget.EasyCoderWidget;
import com.google.gson.JsonObject;
import com.intellij.application.options.editor.EditorOptionsProvider;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class EasyCoderSettingsProvider implements EditorOptionsProvider {
    private SettingsPanel settingsPanel;

    @Override
    public @NotNull @NonNls String getId() {
        return "EasyCoder.Settings";
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "EasyCoder";
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (Objects.isNull(settingsPanel)) {
            settingsPanel = new SettingsPanel();
        }
        return settingsPanel.getPanel();
    }

    @Override
    public boolean isModified() {
        EasyCoderSettings savedSettings = EasyCoderSettings.getInstance();
        return !savedSettings.getServerAddressURL().equals(settingsPanel.getServerAddressUrl())
                || savedSettings.getTabActionOption() != settingsPanel.getTabActionOption()
                || savedSettings.isSaytEnabled() != settingsPanel.getEnableSAYTCheckBox()
                || savedSettings.isCPURadioButtonEnabled() != settingsPanel.getCPUModelRadioButton()
                || savedSettings.isGPURadioButtonEnabled() != settingsPanel.getGPUModelRadioButton()
                || savedSettings.getCompletionMaxToken() != settingsPanel.getCompletionMaxTokens()
                || savedSettings.getChatMaxToken() != settingsPanel.getChatMaxTokens();
    }

    @Override
    public void apply() {
        EasyCoderSettings savedSettings = EasyCoderSettings.getInstance();
        savedSettings.setServerAddressURL(settingsPanel.getServerAddressUrl());
        savedSettings.setSaytEnabled(settingsPanel.getEnableSAYTCheckBox());
        savedSettings.setCPURadioButtonEnabled(settingsPanel.getCPUModelRadioButton());
        savedSettings.setGPURadioButtonEnabled(settingsPanel.getGPUModelRadioButton());
        savedSettings.setTabActionOption(settingsPanel.getTabActionOption());
        savedSettings.setCompletionMaxToken(settingsPanel.getCompletionMaxTokens());
        savedSettings.setChatMaxToken(settingsPanel.getChatMaxTokens());

        for (Project openProject : ProjectManager.getInstance().getOpenProjects()) {
            WindowManager.getInstance().getStatusBar(openProject).updateWidget(EasyCoderWidget.ID);
        }

        JsonObject jsonObject = new JsonObject();
        if(EasyCoderSettings.getInstance().isCPURadioButtonEnabled()){
            jsonObject.addProperty("sendUrl", EasyCoderSettings.getInstance().getServerAddressURL() + EasyCoderURI.CPU_CHAT.getUri());
            jsonObject.addProperty("modelType", "CPU");
        }else{
            jsonObject.addProperty("sendUrl", EasyCoderSettings.getInstance().getServerAddressURL() + EasyCoderURI.GPU_CHAT.getUri());
            jsonObject.addProperty("modelType", "GPU");
        }
        jsonObject.addProperty("maxToken", EasyCoderSettings.getInstance().getChatMaxToken().getDescription());
        JsonObject result = new JsonObject();
        result.addProperty("data", jsonObject.toString());
        Project project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContextFromFocus().getResultSync());
        (project.getService(EasyCoderSideWindowService.class)).notifyIdeAppInstance(result);
    }

    @Override
    public void reset() {
        EasyCoderSettings savedSettings = EasyCoderSettings.getInstance();
        settingsPanel.setServerAddressUrl(savedSettings.getServerAddressURL());
        settingsPanel.setEnableSAYTCheckBox(savedSettings.isSaytEnabled());
        settingsPanel.setTabActionOption(savedSettings.getTabActionOption());
        settingsPanel.setCPUModelRadioButton(savedSettings.isCPURadioButtonEnabled());
        settingsPanel.setGPUModelRadioButton(savedSettings.isGPURadioButtonEnabled());
        settingsPanel.setCompletionMaxTokens(savedSettings.getCompletionMaxToken());
        settingsPanel.setChatMaxTokens(savedSettings.getChatMaxToken());
    }
}
