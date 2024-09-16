package com.easycoder.intellij.widget;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.easycoder.intellij.constant.Const;
import com.easycoder.intellij.http.HttpToolkits;
import com.easycoder.intellij.settings.EasyCoderSettings;
import com.easycoder.intellij.utils.EasyCoderIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Consumer;

public class DynamicStatusBarWidget
        implements StatusBarWidget, StatusBarWidget.MultipleTextValuesPresentation {
    private final Project project;
    private String text = "EasyCoder";
    private String tooltipText = "EasyCoder Status";
    private Icon currentIcon = EasyCoderIcons.WidgetEnabled;

    public DynamicStatusBarWidget(Project project) {
        this.project = project;
    }

    @NotNull
    @Override
    public String ID() {
        return "DynamicStatusBarWidget";
    }

    @Nullable
    @Override
    public WidgetPresentation getPresentation() {
        return this;
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
    }

    @Override
    public void dispose() {
    }

    @NotNull
    public String getText() {
        return text;
    }

    @Nullable
    @Override
    public String getTooltipText() {
        return tooltipText;
    }

    @Nullable
    @Override
    public Consumer<MouseEvent> getClickConsumer() {
        return mouseEvent -> {
            DataContext dataContext = DataContext.EMPTY_CONTEXT;
            ListPopup popup = createPopup(dataContext);
            if (popup != null) {
                showPopupAboveStatusBar(popup, mouseEvent);
            }
        };
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return currentIcon;
    }

    public void updateWidget(String newText, String newTooltipText) {
        this.text = newText;
        this.tooltipText = newTooltipText;
        this.currentIcon = EasyCoderIcons.WidgetEnabled;
        if (project != null && !project.isDisposed()) {
            StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
            if (statusBar != null) {
                statusBar.updateWidget(ID());
            }
        }
    }

    @Nullable
    protected ListPopup createPopup(DataContext dataContext) {
        boolean isLoginIn = PropertiesComponent.getInstance().getValue("easycoder:token") != null;
        String firstOption = isLoginIn ? Const.LOGIN_OUT : Const.LOGIN_IN;

        BaseListPopupStep<String> firstStep = new BaseListPopupStep<>(tooltipText, Arrays.asList(firstOption)) {
            @Override
            public @Nullable PopupStep<?> onChosen(String selectedValue, boolean finalChoice) {
                if (Const.LOGIN_IN.equals(selectedValue)) {
                    ApplicationManager.getApplication().executeOnPooledThread(() -> openWebpage());
                } else if (Const.LOGIN_OUT.equals(selectedValue)) {
                    HttpToolkits.signOut(project);
                }
                ApplicationManager.getApplication().invokeLater(() -> updateWidget("Status updated", "Status updated"));
                return FINAL_CHOICE;
            }
        };

        return JBPopupFactory.getInstance().createListPopup(firstStep);
    }

    private void showPopupAboveStatusBar(ListPopup popup, MouseEvent mouseEvent) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar != null) {
            Dimension popupSize = popup.getContent().getPreferredSize();
            java.awt.Point locationOnScreen = mouseEvent.getComponent().getLocationOnScreen();
            int x = locationOnScreen.x;
            int y = locationOnScreen.y - popupSize.height;
            popup.show(new RelativePoint(new java.awt.Point(x, y)));
        }
    }

    private void openWebpage() {
        updateWidget("Logging in...", "Please wait");

        String uuid = UUID.randomUUID().toString();
        String serverAddress = EasyCoderSettings.getInstance().getServerAddressShaun();
        String targetUrl = serverAddress + "?sessionId=" + uuid;
        BrowserUtil.browse(targetUrl);

        CompletableFuture<Map<String, String>> future = HttpToolkits.fetchToken(uuid);
        future.whenComplete((map, throwable) -> {
            try {
                if (throwable == null && map != null) {
                    HttpToolkits.signIn(project, map);
                    ApplicationManager.getApplication()
                            .invokeLater(() -> updateWidget("Logged in", "EasyCoder is active"));
                } else {
                    ApplicationManager.getApplication()
                            .invokeLater(() -> updateWidget("Login failed", "Please try again"));
                }
            } finally {
                // 如果需要其他清理操作，可以在这里添加
            }
        });
    }

    @Override
    public @Nullable("null means the widget is unable to show the popup") ListPopup getPopupStep() {
        return null;
    }

    @Nullable
    @Override
    public String getSelectedValue() {
        return text;
    }
}
