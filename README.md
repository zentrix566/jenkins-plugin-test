# Simple API Remote Trigger - Jenkins 插件

Jenkins 简单 API 远程触发插件。通过 HTTP API + Token 触发构建，支持一个 Token 触发多个任务。

## 🎯 功能特性

- ✅ **统一入口触发**：一个 URL 触发所有配置了相同 Token 的 Jenkins 任务
- ✅ **支持所有任务类型**：自由风格项目 **和** Pipeline 流水线项目都支持
- ✅ **匿名访问**：不需要 Jenkins 账号认证就能调用（只靠 Token 验证）
- ✅ **轻量级**：代码简单，没有额外依赖
- ✅ **多任务批量触发**：一次 API 调用可以触发多个任务同时构建

## 🆚 和 Jenkins 原生 "触发远程构建" 对比

| 特性 | 原生 "触发远程构建" | 本插件 |
|------|------------------|--------|
| 触发入口 | 每个任务一个 URL：`/job/XXX/build?token=YYY` | 统一入口：`/api-trigger/invoke?token=YYY` |
| 批量触发 | 需要多次调用多个 URL | 一次调用触发所有匹配 Token 的任务 |
| 外部配置 | 增减任务需要修改外部调用地址 | 只需要在 Jenkins 里改 Token，外部地址不变 |
| 适用场景 | 单个任务独立触发 | Git/GitLab webhook 触发多个微服务构建 |

## 📦 安装

### 方式一：从文件上传（推荐）

1. 下载 `target/simple-api-trigger.hpi`
2. 打开 Jenkins → 系统管理 → 插件管理 → 高级 → 上传插件
3. 选择 `.hpi` 文件，上传，重启 Jenkins

### 方式二：本地编译构建

```bash
# 环境配置（Windows Git Bash）
export JAVA_HOME="/c/Users/EDY/.jdks/ms-17.0.17"
export MAVEN_HOME="/e/apache-maven-3.9.14"
export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"

# 编译打包（跳过测试和许可证检查）
mvn clean compile hpi:hpi -DskipTests -Dlicense.skip=true
```

编译产物：`target/simple-api-trigger.hpi`

## 🚀 使用方式

### 1. 配置任务

1. 打开你的 Jenkins 任务 → 点击 **配置**
2. 往下翻找到 **构建触发器** 区域
3. 勾选 **API 远程触发器 (API Remote Trigger)**
4. 在 **验证 Token (Auth Token)** 输入框填入你的 Token（例如：`my-webhook-token`）
5. 点击 **保存** 配置

> 💡 提示：多个任务可以配置同一个 Token，调用一次就会全部触发，非常适合微服务场景！

### 2. 调用 API 触发

```
GET http://jenkins-host:port/api-trigger/invoke?token=YOUR_TOKEN
```

### 响应示例

**成功（触发 1 个构建）：**
```
成功。已触发 1 个项目进行构建。
```

**成功（触发 3 个构建）：**
```
成功。已触发 3 个项目进行构建。
```

**Token 不匹配：**
```
失败。未找到具有匹配 Token 'wrong-token' 的项目。
HTTP 状态码：404
```

**缺少 token 参数：**
```
必需参数 'token' 缺失。
HTTP 状态码：400
```

## 📝 使用示例

### GitLab/GitHub Webhook 配置

如果你有多个微服务在同一个 Git 仓库，想在 Push 时触发多个 Jenkins 构建：

1. 在每个微服务对应的 Jenkins 任务中，都配置**同一个 Token**，例如 `gitlab-push`
2. 在 GitLab/GitHub 的 Webhook 配置中，填入：
   ```
   https://your-jenkins.com/api-trigger/invoke?token=gitlab-push
   ```
3. 当 Git 收到 Push 时，会一次调用触发所有配置了该 Token 的任务

### 使用 curl 触发

```bash
curl "http://127.0.0.1:8080/api-trigger/invoke?token=your-token"
```

## 🔒 安全说明

- 本插件允许**匿名访问**，只要 Token 正确就能触发
- Token 请使用足够复杂的随机字符串，防止被猜测
- 如果你的 Jenkins 在公网，请确保 Token 安全

## 📋 开发环境

- Java: 17
- Maven: 3.9.14
- 目标 Jenkins 版本: 2.361.4+

## 📄 许可证

MIT
