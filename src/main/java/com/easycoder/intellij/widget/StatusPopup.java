package com.easycoder.intellij.widget;

import com.easycoder.intellij.constant.Const;
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
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class StatusPopup extends EditorBasedStatusBarPopup {

    public StatusPopup(@NotNull Project project) {
        super(project, false);
    }

    @NotNull
    @Override
    protected WidgetState getWidgetState(@Nullable VirtualFile virtualFile) {
        return new WidgetState("Click to see more", "EasyCoder", true);
    }

    @Override
    protected @Nullable ListPopup createPopup(DataContext dataContext) {
        boolean isLoginIn = "true".equals(PropertiesComponent.getInstance().getValue("easycoder.is-login"));
        String firstOption = isLoginIn ? Const.LOGIN_OUT : Const.LOGIN_IN;

        BaseListPopupStep<String> firstStep = new BaseListPopupStep<>("EasyCoder Status", Arrays.asList(firstOption)) {
            @Override
            public @Nullable PopupStep<?> onChosen(String selectedValue, boolean finalChoice) {
                if (Const.LOGIN_IN.equals(selectedValue)) {
                    PropertiesComponent.getInstance().setValue("easycoder.is-login", true);
//                    openWebpage();
                } else if (Const.LOGIN_OUT.equals(selectedValue)) {
                    PropertiesComponent.getInstance().setValue("easycoder.is-login", false);
                }
                update();  // Refresh the status bar widget to reflect the new state
                return FINAL_CHOICE;
            }
        };

        return JBPopupFactory.getInstance().createListPopup(firstStep);
    }

    private void openWebpage() {
        BrowserUtil.browse(Const.WEBSITE);
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
