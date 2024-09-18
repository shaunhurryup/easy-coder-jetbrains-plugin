package com.easycoder.intellij.widget;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.easycoder.intellij.constant.Const;
import com.easycoder.intellij.http.HttpToolkits;
import com.easycoder.intellij.notification.ModalHelper;
import com.easycoder.intellij.settings.EasyCoderSettings;
import com.easycoder.intellij.utils.EasyCoderIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.status.IdeStatusBarImpl;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Consumer;

public class DynamicStatusBarWidget
        implements StatusBarWidget, StatusBarWidget.TextPresentation {
    private final Project project;
    private String text = "Ready";
    private String tooltipText = "I am ready to help you";
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

    @Nullable
    @Override
    public String getTooltipText() {
        return tooltipText;
    }

    @Nullable
    @Override
    public Consumer<MouseEvent> getClickConsumer() {
        return mouseEvent -> {
            JBPopup popup = getPopup();
            if (popup != null) {
                showPopupAboveStatusBar(popup, mouseEvent);
            }
        };
    }

    // @Nullable
    // @Override
    // public Icon getIcon() {
    // return currentIcon;
    // }

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
    public JBPopup getPopup() {
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
                return FINAL_CHOICE;
            }
        };

        return JBPopupFactory.getInstance().createListPopup(firstStep);
    }

    private void showPopupAboveStatusBar(JBPopup popup, MouseEvent mouseEvent) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar != null) {
            JComponent widgetComponent = ((IdeStatusBarImpl) statusBar).getWidgetComponent(ID());

            Point widgetOnScreen = widgetComponent.getLocationOnScreen();
            Dimension popupSize = popup.getContent().getPreferredSize();

            Component statusBarComponent = statusBar.getComponent();
            Point statusBarLocation = statusBarComponent.getLocationOnScreen();

            // 计算相对于状态栏的x坐标
            int relativeX = widgetOnScreen.x - statusBarLocation.x;
            int relativeY = widgetOnScreen.y - statusBarLocation.y - popupSize.height;

            // 确保 Popup 不超出屏幕边界
            // 这里可以根据需要添加边界检查

            // 设置 Popup 的位置，使其左侧与按钮左侧对齐
            popup.show(new RelativePoint(statusBarComponent, new Point(relativeX, relativeY)));
        }
    }

    private void openWebpage() {
        String uuid = UUID.randomUUID().toString();

        String serverAddress = EasyCoderSettings.getInstance().getServerAddressShaun();
        if (!isValidUrl(serverAddress)) {
            ModalHelper.showWarning(
                project,
                "Please enter a valid server address in the settings.",
                "Invalid Server Address"
            );
            return;
        }

        String targetUrl = serverAddress + "?sessionId=" + uuid;
        BrowserUtil.browse(targetUrl);

        CompletableFuture<Map<String, String>> future = HttpToolkits.fetchToken(uuid);
        future.whenComplete((map, throwable) -> {
            try {
                if (throwable == null && map != null) {
                    updateWidget("Ready", "");
                    HttpToolkits.signIn(project, map);
                }
            } finally {
            }
        });
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public float getAlignment() {
        return 0;
    }

    private boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
