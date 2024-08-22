package com.easycoder.intellij.enums;

public enum MessageId {
    // Placeholder to avoid switch-case issues with the first enum
    PLACEHOLDER,

    // Authentication and user management
    OpenSignInWebpage,        // Initiate plugin login
    SuccessfulAuth,           // VSCode account authentication successful
    SignOutExtension,         // Sign out from VSCode account

    // Webview initialization and management
    WebviewMount,             // Triggered after Webview component mount
    WebviewInitQaExamples,    // Initialize technical Q&A example questions
    WebviewInitToolkits,      // Initialize toolbox list
    GetExtensionSettings,     // Retrieve plugin configuration
    ToggleColorTheme,         // Switch color theme

    // Question and answer interactions
    WebviewQuestion,          // Ask a question from Webview input box
    WebviewCommandQuestion,   // Ask a question from Webview input box options
    WebviewToolkitQuestion,   // Ask a question from toolbox
    WebviewCodeTranslation,   // Code translation request
    ReGenerateAnswer,         // Regenerate an answer
    ReactOnQaResponse,        // Like/dislike GPT's answer in technical Q&A
    WebviewAbortQa,           // Interrupt technical Q&A

    // History management
    GetHistoryDialogs,        // Retrieve history dialogs
    GetHistoryDialogDetail,   // Get details of a specific history dialog
    RemoveDialog32,           // Remove a dialog (purpose of '32' is unclear)

    // Editor interactions
    SetSelectedText,          // Set editor selected text
    ClearSelectedText,        // Clear editor selected text
    InsertIntoEditor,         // Insert Webview content into Editor
    ReplaceEditorText,        // Replace Editor content with Webview content
    CopyToClipboard,          // Copy content to clipboard

    // Menu-triggered actions
    ExplainCode_Menu,         // Explain code (menu option)
    OptimizeCode_Menu,        // Optimize code (menu option)
    GenerateComment_Menu,     // Generate comments (menu option)
    GenerateUnitTest_Menu,    // Generate unit tests (menu option)
    CheckPerformance_Menu,    // Check performance (menu option)
    CheckSecurity_Menu,       // Check security (menu option)

    // Notifications and messages
    ShowErrorMessage,         // Create VSCode error message popup from Webview
    ShowWarnMessage,          // Create VSCode warning message popup from Webview
    ShowInfoMessage,          // Create VSCode info message popup from Webview
    ToastWarning,             // Display a warning toast
    CatchWebviewError,        // Global exception catcher for Webview
}