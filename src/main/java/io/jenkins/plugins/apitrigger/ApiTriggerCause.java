package io.jenkins.plugins.apitrigger;

import hudson.model.Cause;

public class ApiTriggerCause extends Cause {
    private final String tokenUsed;

    public ApiTriggerCause(String tokenUsed) {
        this.tokenUsed = tokenUsed;
    }

    @Override
    public String getShortDescription() {
        // 这里的文本会出现在构建历史的“Started by...”中
        return "由外部 API 触发 (使用 Token: " + tokenUsed + ")";
    }
}