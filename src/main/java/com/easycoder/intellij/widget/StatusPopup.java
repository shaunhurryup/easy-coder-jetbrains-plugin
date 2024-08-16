package com.easycoder.intellij.widget;

import com.easycoder.intellij.constant.Const;
import com.easycoder.intellij.enums.MessageId;
import com.easycoder.intellij.http.HttpToolkits;
import com.easycoder.intellij.services.EasyCoderSideWindowService;
import com.google.gson.JsonObject;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup;
import org.apache.commons.httpclient.HttpURL;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StatusPopup extends EditorBasedStatusBarPopup {
    Project project;

    public StatusPopup(@NotNull Project project) {
        super(project, false);
        this.project = project;
    }

    @NotNull
    @Override
    protected WidgetState getWidgetState(@Nullable VirtualFile virtualFile) {
        return new WidgetState("Click to see more", "EasyCoder", true);
    }

    @Override
    protected @Nullable ListPopup createPopup(DataContext dataContext) {
        boolean isLoginIn = PropertiesComponent.getInstance().getValue("easycoder:token") != null;
        String firstOption = isLoginIn ? Const.LOGIN_OUT : Const.LOGIN_IN;

        BaseListPopupStep<String> firstStep = new BaseListPopupStep<>("EasyCoder Status", Arrays.asList(firstOption)) {
            @Override
            public @Nullable PopupStep<?> onChosen(String selectedValue, boolean finalChoice) {
                if (Const.LOGIN_IN.equals(selectedValue)) {
                    openWebpage();
                } else if (Const.LOGIN_OUT.equals(selectedValue)) {
                    HttpToolkits.signOut(project);
                }
                update();  // Refresh the status bar widget to reflect the new state
                return FINAL_CHOICE;
            }
        };

        return JBPopupFactory.getInstance().createListPopup(firstStep);
    }

    private void openWebpage() {
        String uuid = UUID.randomUUID().toString();
        String targetUrl = Const.WEBSITE + "?sessionId=" + uuid;
        BrowserUtil.browse(targetUrl);

        CompletableFuture<Map<String, String>> future = HttpToolkits.fetchToken(uuid);
        future.thenAccept(map -> {
            if (map != null) {
                HttpToolkits.signIn(project, map);
            }
        });
    }

    @Override
    protected @NotNull StatusBarWidget createInstance(@NotNull Project project) {
        return new StatusPopup(project);
    }

    @Override
    public @NonNls @NotNull String ID() {
        return "easycoder.status.popup";
    }
}
