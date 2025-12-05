# 智能表单助手

基于 Spring Boot 和 LLM 的智能表单生成系统，通过自然语言对话自动识别用户意图并生成相应的业务表单。

## 功能特性

### 核心功能

1. **智能意图识别**
   - 使用 LLM（支持 DeepSeek、Groq、OpenAI 等）进行自然语言意图识别
   - 支持三种意图类型：
     - `create_form`：创建新表单
     - `fill_form`：填写现有表单
     - `chat`：普通对话

2. **支持的业务表单类型**
   - **订酒店**：酒店预订表单
   - **定机票**：机票预订表单
   - **请假**：请假申请表单
   - **报销发票**：发票报销表单

3. **智能表单生成**
   - 根据业务意图自动生成表单字段
   - 支持多种字段类型：文本、日期、数字、邮箱、电话、下拉选择等
   - 自动字段类型映射（中英文类型名称自动转换为标准HTML类型）
   - 支持必填字段、默认值、占位符等

4. **表单填写与更新**
   - 支持通过自然语言填写表单字段
   - 支持增量更新表单数据
   - 表单数据实时保存到会话

5. **意图验证**
   - 自动验证业务意图，防止误生成表单
   - 只有明确的业务意图才会创建表单
   - 自我介绍、闲聊等不会触发表单生成

6. **会话管理**
   - 多会话支持，每个会话独立的表单状态
   - 会话级别的表单字段和数据管理
   - 支持清除会话

## 技术栈

- **后端框架**：Spring Boot 3.2.0
- **Java 版本**：Java 21
- **LLM 集成**：OpenAI 兼容 API（支持 DeepSeek、Groq、OpenAI 等）
- **HTTP 客户端**：OkHttp 4.12.0
- **JSON 处理**：Jackson
- **前端**：HTML/CSS/JavaScript（单页应用）

## 项目结构

```
form-assistant/
├── src/
│   ├── main/
│   │   ├── java/com/formdemo/
│   │   │   ├── controller/          # REST 控制器
│   │   │   │   ├── ChatController.java      # 聊天和表单API
│   │   │   │   └── IndexController.java     # 首页路由
│   │   │   ├── service/             # 业务服务
│   │   │   │   ├── ChatService.java         # 聊天业务逻辑
│   │   │   │   ├── LocalIntentService.java  # 意图识别服务
│   │   │   │   ├── OpenAIService.java       # LLM API调用服务
│   │   │   │   ├── FormGeneratorService.java # 表单生成服务
│   │   │   │   └── UserInfoService.java     # 用户信息服务
│   │   │   ├── model/               # 数据模型
│   │   │   │   ├── ChatMessage.java         # 聊天消息模型
│   │   │   │   ├── ChatResponse.java        # 聊天响应模型
│   │   │   │   ├── FormField.java           # 表单字段模型
│   │   │   │   ├── LLMIntentResponse.java   # LLM意图响应模型
│   │   │   │   └── ...
│   │   │   └── exception/           # 异常类
│   │   │       └── OpenAIException.java     # LLM API异常
│   │   └── resources/
│   │       ├── static/              # 前端静态资源
│   │       │   └── index.html       # 聊天界面
│   │       ├── application.yml      # 应用配置
│   │       └── application.properties # API配置
│   └── test/                        # 测试用例
└── pom.xml
```

## 快速开始

### 环境要求

- Java 21+
- Maven 3.6+

### 配置 LLM API

#### 方式一：使用 DeepSeek API（推荐，免费）

1. **注册并获取 API Key**
   - 访问：https://platform.deepseek.com
   - 注册账号并创建 API Key

2. **配置 API Key**
   
   编辑 `src/main/resources/application.properties`：
   ```properties
   api-key=sk-您的DeepSeek-API-Key
   api-url=https://api.deepseek.com/v1/chat/completions
   api-model=deepseek-chat
   ```
   
   或使用环境变量：
   ```bash
   export api-key=sk-您的DeepSeek-API-Key
   export api-url=https://api.deepseek.com/v1/chat/completions
   export api-model=deepseek-chat
   ```

#### 方式二：使用其他 LLM 服务

**Groq API（超快速度）：**
```properties
api-key=您的Groq-API-Key
api-url=https://api.groq.com/openai/v1/chat/completions
api-model=llama-3.1-70b-versatile
```

**OpenAI API（付费）：**
```properties
api-key=sk-您的OpenAI-API-Key
api-url=https://api.openai.com/v1/chat/completions
api-model=gpt-3.5-turbo
```

**Ollama（本地，完全免费）：**
```properties
api-key=ollama
api-url=http://127.0.0.1:11434/v1/chat/completions
api-model=qwen2.5
```

### 启动应用

```bash
# 编译项目
mvn clean install

# 启动应用
mvn spring-boot:run
```

访问：http://localhost:8080

## 使用指南

### 创建表单

用户可以通过自然语言表达业务意图，系统会自动生成相应的表单：

**示例：**
- "我要订酒店"
- "帮我定一张机票"
- "我想请假"
- "申请报销发票"

系统会：
1. 识别业务意图
2. 生成相应的表单字段
3. 在聊天界面显示表单

### 填写表单

创建表单后，可以通过自然语言填写字段：

**示例：**
- "把姓名填成张三"
- "目的地填北京"
- "入住日期选明天"

系统会：
1. 识别要更新的字段
2. 更新表单数据
3. 刷新表单显示

### 聊天对话

对于非业务意图的对话，系统会进行友好回复，不会生成表单：

**示例：**
- "我叫jeffery" → 回复支持的表单类型提示
- "你好" → 友好问候
- "今天天气怎么样" → 普通对话

## API 接口

### 1. 发送消息

```http
POST /api/chat/message
Content-Type: application/json

{
  "message": "我要订酒店",
  "sessionId": "session_123"
}
```

**响应示例：**
```json
{
  "responseText": "好的，我已经为您创建了表单，请填写以下信息：",
  "formHtml": "<div class=\"form-container\">...</div>",
  "hasForm": true,
  "formId": "uuid-form-id",
  "intentType": "create_form",
  "needsClarification": false
}
```

### 2. 提交表单

```http
POST /api/chat/form/submit
Content-Type: application/json

{
  "name": "张三",
  "destination": "北京",
  "checkInDate": "2024-01-01",
  "formId": "form-id"
}
```

**响应：**
```json
{
  "success": true,
  "message": "表单提交成功！",
  "data": { ... }
}
```

### 3. 清除会话

```http
DELETE /api/chat/session/{sessionId}
```

## 配置说明

### application.properties

```properties
# LLM API 配置
api-key=sk-您的API-Key
api-url=https://api.deepseek.com/v1/chat/completions
api-model=deepseek-chat
```

### application.yml

```yaml
server:
  port: 8080

spring:
  application:
    name: form-demo

logging:
  level:
    com.formdemo: DEBUG
    root: INFO
```

## 字段类型映射

系统内置了字段类型映射表，支持中英文类型名称自动转换：

| 中文/英文输入 | 标准HTML类型 |
|-------------|-------------|
| 日期/date | date |
| 时间/datetime | datetime-local |
| 姓名/name | text |
| 邮箱/email | email |
| 电话/phone | tel |
| 数字/number | number |
| 文本/text | text |
| 多行文本/textarea | textarea |
| 密码/password | password |
| 选择/select | select |
| 复选框/checkbox | checkbox |
| 单选/radio | radio |

## 支持的字段类型

- **text**：单行文本
- **textarea**：多行文本
- **email**：邮箱
- **tel**：电话
- **number**：数字
- **date**：日期
- **datetime-local**：日期时间
- **select**：下拉选择
- **checkbox**：复选框
- **radio**：单选按钮
- **password**：密码

## 业务意图关键词

### 订酒店
- 订酒店、预订酒店、酒店预订、定酒店、订房、预订房间

### 定机票
- 定机票、订机票、预订机票、机票预订、买机票、购票

### 请假
- 请假、申请请假、请假申请、请年假、请病假、申请休假

### 报销发票
- 报销、报销发票、发票报销、报销申请、申请报销、费用报销

## 测试

### 运行所有测试

```bash
mvn test
```

### 运行特定测试

```bash
# 测试 DeepSeek LLM 连接
mvn test -Dtest=DeepSeekLLMTest#testDeepSeekAPIConnection

# 测试意图识别
mvn test -Dtest=DeepSeekLLMTest#testIntentRecognition
```

## 部署

### 本地运行

```bash
mvn spring-boot:run
```

### 打包为 JAR

```bash
mvn clean package
java -jar target/form-assistant-1.0.0.jar
```

### Docker 部署（可选）

```dockerfile
FROM openjdk:21-jdk-slim
COPY target/form-assistant-1.0.0.jar app.jar
ENV api-key=您的API-Key
ENV api-url=https://api.deepseek.com/v1/chat/completions
ENV api-model=deepseek-chat
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 常见问题

### 1. API Key 认证失败

**问题**：返回 401 错误

**解决**：
- 检查 API Key 是否正确
- 确认 API Key 是否已激活
- 验证 API URL 是否正确

### 2. 无法连接到 LLM 服务

**问题**：连接超时或连接拒绝

**解决**：
- 检查网络连接
- 验证 API URL 是否正确
- 如果使用 Ollama，确保服务正在运行

### 3. 意图识别不准确

**问题**：错误识别为创建表单

**解决**：
- 系统已内置验证逻辑，会自动过滤非业务意图
- 确保使用明确的业务关键词

## 开发指南

### 添加新的业务意图

1. 在 `ChatService.isValidBusinessIntent()` 中添加关键词
2. 在 `LocalIntentService.buildIntentPrompt()` 中更新提示词

### 自定义字段类型映射

编辑 `LocalIntentService.FIELD_TYPE_MAP` 添加新的映射关系。

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！

---

**享受使用智能表单助手！** 🚀
