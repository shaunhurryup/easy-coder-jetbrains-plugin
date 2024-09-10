package com.easycoder.intellij.widget;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.easycoder.intellij.constant.Const;
import com.easycoder.intellij.handlers.GlobalStore;
import com.easycoder.intellij.http.HttpToolkits;
import com.easycoder.intellij.settings.EasyCoderSettings;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.impl.status.EditorBasedStatusBarPopup;

public class StatusPopup extends EditorBasedStatusBarPopup {
    private Project project;
    public static final String ID = "StatusPopupFactory";

    public StatusPopup(@NotNull Project project) {
        super(project, false);
        this.project = project;
    }

    @NotNull
    @Override
    protected WidgetState getWidgetState(@Nullable VirtualFile virtualFile) {
        boolean loading = GlobalStore.loading;
        System.out.println("globalStore: " + GlobalStore.status + GlobalStore.text + GlobalStore.tooltip);
        return new WidgetState("This is tooltip", loading ? "Loading..." : "EasyCoder", true);
    }

    @Override
    protected @Nullable ListPopup createPopup(DataContext dataContext) {
        boolean isLoginIn = PropertiesComponent.getInstance().getValue("easycoder:token") != null;
        String firstOption = isLoginIn ? Const.LOGIN_OUT : Const.LOGIN_IN;

        BaseListPopupStep<String> firstStep = new BaseListPopupStep<>("EasyCoder Status", Arrays.asList(firstOption)) {
            @Override
            public @Nullable PopupStep<?> onChosen(String selectedValue, boolean finalChoice) {
                if (Const.LOGIN_IN.equals(selectedValue)) {
                    ApplicationManager.getApplication().executeOnPooledThread(() -> openWebpage());
                } else if (Const.LOGIN_OUT.equals(selectedValue)) {
                    HttpToolkits.signOut(project);
                }
                ApplicationManager.getApplication().invokeLater(() -> update());
                return FINAL_CHOICE;
            }
        };

        return JBPopupFactory.getInstance().createListPopup(firstStep);
    }

    private void openWebpage() {
        String uuid = UUID.randomUUID().toString();
        
        String serverAddress = EasyCoderSettings.getInstance().getServerAddressShaun();
        String targetUrl = serverAddress + "?sessionId=" + uuid;
        BrowserUtil.browse(targetUrl);

        CompletableFuture<Map<String, String>> future = HttpToolkits.fetchToken(uuid);
        future.whenComplete((map, throwable) -> {
            try {
                if (throwable == null && map != null) {
                    HttpToolkits.signIn(project, map);
                }
            } finally {
                ApplicationManager.getApplication().invokeLater(() -> update());
            }
        });
    }

    @Override
    protected @NotNull StatusBarWidget createInstance(@NotNull Project project) {
        return new StatusPopup(project);
    }

    @Override
    public @NonNls @NotNull String ID() {
        return ID;
    }
}
