package io.jenkins.plugins.apitrigger;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.Item;
import hudson.model.Job;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.logging.Logger;

/**
 * 核心触发器类。继承 Trigger<Job<?, ?>> 以支持所有类型的 Job（包括 Pipeline）。
 */
public class ApiBuildTrigger extends Trigger<Job<?, ?>> {

    private static final Logger LOGGER = Logger.getLogger(ApiBuildTrigger.class.getName());

    private final String authToken; // 用于 UI 绑定的 Token

    // 1. DataBoundConstructor：用于将 Jelly 界面上的配置数据绑定到 Java 对象
    @DataBoundConstructor
    public ApiBuildTrigger(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthToken() {
        return authToken;
    }

    // 2. 核心方法：由 Tracker 调用来真正触发构建
    public void performTrigger() {
        if (!(job instanceof Job)) {
            LOGGER.warning("API Trigger: job is not a Job instance, skipping");
            return;
        }
        Job<?, ?> j = (Job<?, ?>) job;

        LOGGER.info(() -> "API Trigger: 正在为项目 [" + j.getFullName() + "] 排队构建...");

        Cause cause = new ApiTriggerCause(authToken);
        boolean triggered = false;

        // 处理 AbstractProject (自由风格)
        if (j instanceof AbstractProject) {
            ((AbstractProject<?, ?>) j).scheduleBuild2(0, cause);
            triggered = true;
        } else {
            // 对 Pipeline (WorkflowJob) 和其他 Job 类型，尝试多种方法
            try {
                // 1. 先试 scheduleBuild(Cause...)
                try {
                    java.lang.reflect.Method method = j.getClass().getMethod("scheduleBuild", int.class, Cause[].class);
                    method.invoke(j, 0, new Cause[]{cause});
                    triggered = true;
                } catch (NoSuchMethodException e1) {
                    // 2. 再试 scheduleBuild(Cause...) without delay
                    try {
                        java.lang.reflect.Method method = j.getClass().getMethod("scheduleBuild", Cause[].class);
                        method.invoke(j, (Object) new Cause[]{cause});
                        triggered = true;
                    } catch (NoSuchMethodException e2) {
                        // 3. 再试 scheduleBuild2(int, Cause...)
                        try {
                            java.lang.reflect.Method method = j.getClass().getMethod("scheduleBuild", int.class, Cause.class);
                            method.invoke(j, 0, cause);
                            triggered = true;
                        } catch (NoSuchMethodException e3) {
                            // 4. 试试 scheduleBuild with just delay
                            try {
                                java.lang.reflect.Method method = j.getClass().getMethod("scheduleBuild", int.class);
                                method.invoke(j, 0);
                                triggered = true;
                            } catch (Exception e4) {
                                LOGGER.log(java.util.logging.Level.WARNING,
                                    "API Trigger: All triggering methods failed for " + j.getFullName(), e4);
                            }
                        } catch (Exception e3) {
                            LOGGER.log(java.util.logging.Level.WARNING,
                                "API Trigger: Failed to trigger build for " + j.getFullName(), e3);
                        }
                    } catch (Exception e2) {
                        LOGGER.log(java.util.logging.Level.WARNING,
                            "API Trigger: Failed to trigger build for " + j.getFullName(), e2);
                    }
                } catch (Exception e1) {
                    LOGGER.log(java.util.logging.Level.WARNING,
                        "API Trigger: Failed to trigger build for " + j.getFullName(), e1);
                }
            } catch (Exception e) {
                LOGGER.log(java.util.logging.Level.WARNING,
                    "API Trigger: Unexpected error triggering build for " + j.getFullName(), e);
            }
        }

        if (!triggered) {
            LOGGER.warning("API Trigger: No triggering method available for " + j.getFullName());
        }
    }

    // 3. 生命周期方法：Job 启动时注册到 Tracker
    @Override
    public void start(Job<?, ?> project, boolean newInstance) {
        super.start(project, newInstance);
        LOGGER.info(() -> "API Trigger: 已在项目 [" + project.getFullName() + "] 中启动。");
        ApiTriggerTracker.getInstance().register(this);
    }

    // 4. 生命周期方法：Job 停止/删除时从 Tracker 注销
    @Override
    public void stop() {
        ApiTriggerTracker.getInstance().unregister(this);
        super.stop();
    }

    // 5. Descriptor：用于在 Jenkins UI 上定义该触发器的元数据
    @Extension
    public static final class DescriptorImpl extends TriggerDescriptor {

        @Override
        public boolean isApplicable(Item item) {
            // 允许该触发器用于任何类型的 Job (包括 WorkflowJob/Pipeline)
            return item instanceof Job;
        }

        @Override
        public String getDisplayName() {
            // 在 Job 配置页面显示的名称
            return "API 远程触发器 (API Remote Trigger)";
        }
    }
}