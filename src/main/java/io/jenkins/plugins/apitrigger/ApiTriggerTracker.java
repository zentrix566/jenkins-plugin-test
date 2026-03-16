package io.jenkins.plugins.apitrigger;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * 全局单例，用于追踪所有激活的触发器实例。
 */
public class ApiTriggerTracker {

    private static final Logger LOGGER = Logger.getLogger(ApiTriggerTracker.class.getName());
    private static final ApiTriggerTracker INSTANCE = new ApiTriggerTracker();

    // 使用 ConcurrentHashMap 确保线程安全
    private final Set<ApiBuildTrigger> activeTriggers = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private ApiTriggerTracker() {}

    public static ApiTriggerTracker getInstance() {
        return INSTANCE;
    }

    public void register(ApiBuildTrigger trigger) {
        activeTriggers.add(trigger);
    }

    public void unregister(ApiBuildTrigger trigger) {
        activeTriggers.remove(trigger);
    }

    /**
     * 根据 Token 查找并触发匹配的 Job。
     * @return 触发成功的 Job 数量。
     */
    public int triggerJobsByToken(String token) {
        int count = 0;
        for (ApiBuildTrigger trigger : activeTriggers) {
            // 验证 Token 是否匹配
            if (token.equals(trigger.getAuthToken())) {
                trigger.performTrigger();
                count++;
            }
        }
        return count;
    }
}