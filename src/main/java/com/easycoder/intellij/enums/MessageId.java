package com.easycoder.intellij.enums;

public enum MessageId {
    // fixed: 在 switch-case 中使用第一个 enum 会报错
    PLACEHOLDER,

    // 技术问答相关
    ReactOnQaResponse, // 对 GPT 的回答点赞/点踩
    WebviewQuestion, // 从 webview 输入框提出问题
    WebviewCommandQuestion, // 从 webview 输入框的选项提出问题
    WebviewInitQaExamples, // 初始化技术问答示例问题
    WebviewAbortQa, // 中断技术问答
    ReGenerateAnswer, // 重新生成回答

    // 代码相关功能
    WebviewCodeTranslation, // 代码翻译
    ExplainCode_Menu, // 解释代码
    OptimizeCode_Menu, // 优化代码
    GenerateComment_Menu, // 生成注释
    GenerateUnitTest_Menu, // 生成单元测试
    CheckPerformance_Menu, // 检查性能
    CheckSecurity_Menu, // 检查安全性

    // 工具箱相关
    WebviewInitToolkits, // 工具箱列表
    WebviewToolkitQuestion, // 工具箱提问

    // 认证相关
    OpenSignInWebpage, // 登录插件
    SuccessfulAuth, // vscode 账号认证通过
    SignOutExtension, // 退出 vscode 账号

    // Webview 相关
    WebviewMount, // Webview 组件挂载后触发
    CatchWebviewError, // Webview 全局异常捕获器

    // 消息展示
    ShowErrorMessage, // Webview 创建 vscode 错误消息弹窗
    ShowWarnMessage, // Webview 创建 vscode 警告消息弹窗
    ShowInfoMessage, // Webview 创建 vscode 通知消息弹窗
    ToastWarning, // 显示警告提示

    // 编辑器操作
    InsertIntoEditor, // 将 Webview 内容插入 Editor
    ReplaceEditorText, // 用 Webview 内容替换 Editor 内容
    SetSelectedText, // 编辑器选中文字
    ClearSelectedText, // 清空编辑器选中文字

    // 历史记录相关
    GetHistoryDialogs, // 获取历史对话列表
    GetHistoryDialogDetail, // 获取历史对话详情
    RemoveDialog32, // 删除对话

    // 其他功能
    GetExtensionSettings, // 获取插件配置
    ToggleColorTheme, // 切换颜色主题
    CopyToClipboard, // 复制到剪贴板
    OpenExternalLink // 打开外部链接
}