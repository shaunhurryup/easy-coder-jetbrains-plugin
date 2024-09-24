package com.easycoder.intellij.enums;

public enum ServiceRoute {
    GET_QA_EXAMPLES("/api/easycoder-api/app/exampleQuestions/list"),
    SEND_QUESTION("/api/easycoder-api/app/session/chat"),
    RE_GENERATE("/api/easycoder-api/app/record/regenerate/"),
    GET_HISTORY_DIALOGS("/api/easycoder-api/app/session/list"),
    GET_DIALOG_DETAIL("/api/easycoder-api/app/session/info/"),
    REMOVE_DIALOG("/api/easycoder-api/app/session/remove/"),
    GET_TOOLKITS("/api/easycoder-api/app/toolbox/list"),
    REACT_ON_QA_RESPONSE("/api/easycoder-api/app/record/like"), // 技术问答 - 对 GPT 的回答点赞/点踩
    CODE_COMPLETION("/api/easycoder-api/app/session/completions"),
    CODE_TRANSLATION("/api/easycoder-api/app/session/translation"),
    ACCEPT_INLINE_COMPLETION("/api/easycoder-api/app/record/accept/"),
    // 知识库
    GET_KNOWLEDGE_REPO_LIST("/api/easycoder-api/app/knowledgeRepository/list"),
    GET_KNOWLEDGE_SESSION_LIST("/api/easycoder-api/app/knowledgeSession/list"),
    GET_KNOWLEDGE_DIALOG_DETAIL("/api/easycoder-api/app/knowledgeSession/info/"),
    KNOWLEDGE_SEND_QUESTION("/api/easycoder-api/app/knowledgeSession/ask"),
    REMOVE_KNOWLEDGE_DIALOG("/api/easycoder-api/app/knowledgeSession/remove/"),
    KNOWLEDGE_DIALOG_LIKE("/api/easycoder-api/app/knowledgeSession/like");

    private final String route;

    ServiceRoute(String route) {
        this.route = route;
    }

    public String getRoute() {
        return this.route;
    }
}
