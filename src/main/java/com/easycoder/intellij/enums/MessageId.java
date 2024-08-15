package com.easycoder.intellij.enums;

public enum MessageId {
    // fixed: 在 switch-case 中使用第一个 enum 会报错
    PLACEHOLDER,
    // 技术问答 - 对 GPT 的回答点赞/点踩
    ReactOnQaResponse,
    // 从 webview 输入框提出问题
    WebviewQuestion,
    // 从 webview 输入框的选项提出问题
    WebviewCommandQuestion,
    // 初始化技术问答示例问题
    WebviewInitQaExamples,
    // 代码翻译
    WebviewCodeTranslation,
    // 工具箱列表
    WebviewInitToolkits,
    // 工具箱提问
    WebviewToolkitQuestion,
    // 中断技术问答
    WebviewAbortQa,
    // 登录插件
    OpenSignInWebpage,
    // vscode 账号认证通过
    SuccessfulAuth,
    // 退出 vscode 账号
    SignOutExtension,
    // Webview 组件挂载后触发
    WebviewMount,
    // Webview 全局异常捕获器
    CatchWebviewError,
    // Webview 创建 vscode 错误消息弹窗
    ShowErrorMessage,
    // Webview 创建 vscode 警告消息弹窗
    ShowWarnMessage,
    // Webview 创建 vscode 通知消息弹窗
    ShowInfoMessage,
    // 将 Webview 内容插入 Editor
    InsertIntoEditor,
    // 用 Webview 内容替换 Editor 内容
    ReplaceEditorText,

    ReGenerateAnswer,
    GetHistoryDialogs,
    GetHistoryDialogDetail,
    RemoveDialog32,
    // 编辑器触发的功能键
    ExplainCode_Menu,
    OptimizeCode_Menu,
    GenerateComment_Menu,
    GenerateUnitTest_Menu,
    CheckPerformance_Menu,
    CheckSecurity_Menu,

    ToastWarning,
    // 编辑器选中文字
    SetSelectedText,
    // 清空编辑器选中文字
    ClearSelectedText,
    // 获取插件配置
    GetExtensionSettings,
    ToggleColorTheme
}
