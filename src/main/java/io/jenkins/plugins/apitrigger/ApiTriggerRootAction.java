package io.jenkins.plugins.apitrigger;

import java.io.IOException;

import javax.servlet.ServletException;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerResponse;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;

/**
 * 暴露 HTTP 接口。可以通过 /api-trigger/invoke 调用。
 */
@Extension
public class ApiTriggerRootAction implements UnprotectedRootAction {

    @Override
    public String getIconFileName() {
        return null; // 不在 Jenkins 主页侧边栏显示图标
    }

    @Override
    public String getDisplayName() {
        return "API Trigger Endpoint";
    }

    @Override
    public String getUrlName() {
        // 接口的基础路径，例如：http://jenkins.example.com/api-trigger/
        return "api-trigger";
    }

    /**
     * 具体的触发接口。
     * 访问路径：GET /api-trigger/invoke?token=YOUR_TOKEN
     */
    public void doInvoke(@QueryParameter("token") String token, StaplerResponse rsp)
            throws IOException, ServletException {

        if (token == null || token.isEmpty()) {
            rsp.sendError(400, "必需参数 'token' 缺失。");
            return;
        }

        // 调用 Tracker 查找并触发 Job
        int count = ApiTriggerTracker.getInstance().triggerJobsByToken(token);

        rsp.setContentType("text/plain");
        if (count > 0) {
            rsp.setContentType("text/plain;charset=UTF-8"); 
            rsp.setCharacterEncoding("UTF-8");
            rsp.getWriter().println("成功。已触发 " + count + " 个项目进行构建。");
        } else {
            rsp.setStatus(404);
            rsp.setContentType("text/plain;charset=UTF-8"); 
            rsp.setCharacterEncoding("UTF-8");
            rsp.getWriter().println("失败。未找到具有匹配 Token '" + token + "' 的项目。");
        }
    }
}