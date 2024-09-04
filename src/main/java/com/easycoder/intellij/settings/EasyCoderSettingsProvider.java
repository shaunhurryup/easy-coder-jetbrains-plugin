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
        return !savedSettings.getServerAddressShaun().equals(settingsPanel.getServerAddressShaun())
                || savedSettings.isEnableCodeCompletionShaun() != settingsPanel.getEnableCodeCompletionShaun()
                || !savedSettings.getCodeCompletionLengthShaun().equals(settingsPanel.getCodeCompletionLengthShaun())
                || !savedSettings.getCodeCompletionDelayShaun().equals(settingsPanel.getCodeCompletionDelayShaun())
                || !savedSettings.getChatMaxTokensShaun().equals(settingsPanel.getChatMaxTokensShaun());
    }

    @Override
    public void apply() {
        EasyCoderSettings savedSettings = EasyCoderSettings.getInstance();
        savedSettings.setServerAddressShaun(settingsPanel.getServerAddressShaun());
        savedSettings.setEnableCodeCompletionShaun(settingsPanel.getEnableCodeCompletionShaun());
        savedSettings.setCodeCompletionLengthShaun(settingsPanel.getCodeCompletionLengthShaun());
        savedSettings.setCodeCompletionDelayShaun(settingsPanel.getCodeCompletionDelayShaun());
        savedSettings.setChatMaxTokensShaun(settingsPanel.getChatMaxTokensShaun());

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
        settingsPanel.setServerAddressShaun(savedSettings.getServerAddressShaun());
        settingsPanel.setEnableCodeCompletionShaun(savedSettings.isEnableCodeCompletionShaun());
        settingsPanel.setCodeCompletionLengthShaun(savedSettings.getCodeCompletionLengthShaun());
        settingsPanel.setCodeCompletionDelayShaun(savedSettings.getCodeCompletionDelayShaun());
        settingsPanel.setChatMaxTokensShaun(savedSettings.getChatMaxTokensShaun());
    }
}
