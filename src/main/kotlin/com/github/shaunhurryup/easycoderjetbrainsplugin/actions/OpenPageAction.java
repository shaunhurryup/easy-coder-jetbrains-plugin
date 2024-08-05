package com.github.shaunhurryup.easycoderjetbrainsplugin.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class OpenPageAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        // 获取当前项目上下文
        Project project = e.getProject();

        // 你可以使用 Messages 来测试按钮
        Messages.showMessageDialog(project, "This is my page!", "Information", Messages.getInformationIcon());

        // 在这里可以调用你的页面渲染逻辑
        // 例如，打开一个自定义的 Tool Window 或者使用 WebView 来渲染一个 HTML 页面
    }
}
