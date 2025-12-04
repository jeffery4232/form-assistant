# 智能表单生成系统

基于SpringBoot和OpenAI的智能表单生成系统，可以根据用户的自然语言输入自动识别意图并生成相应的表单。

## 功能特性

1. **智能意图识别**：使用OpenAI API进行意图识别，支持酒店预订、机票购买、火车票预订等场景
2. **友好对话交互**：当用户意图不明确时，系统会温和地询问以明确需求
3. **用户信息获取**：模拟外部API获取用户基本信息，用于填充表单默认值
4. **动态表单生成**：根据意图类型动态生成HTML表单，包含文本、日期、下拉选择等字段类型
5. **聊天界面**：类似豆包的现代化聊天界面，支持持续对话
6. **表单提交**：每个表单独立，支持编辑和提交功能

## 技术栈

- Java 21
- Spring Boot 3.2.0
- OpenAI API (GPT-3.5-turbo)
- OkHttp (HTTP客户端)
- HTML/CSS/JavaScript (前端)

## 项目结构

```
formDemo/
├── src/
│   ├── main/
│   │   ├── java/com/formdemo/
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/      # REST控制器
│   │   │   ├── model/           # 数据模型
│   │   │   └── service/          # 业务服务
│   │   └── resources/
│   │       ├── static/          # 静态资源（前端）
│   │       └── application.yml  # 应用配置
│   └── test/                    # 测试用例
└── pom.xml
```

## 配置说明

在 `application.yml` 中配置OpenAI API密钥：

```yaml
openai:
  api-key: your-api-key-here
  api-url: https://api.openai.com/v1/chat/completions
  model: gpt-3.5-turbo
```

## 运行项目

1. 确保已安装Java 21和Maven
2. 配置OpenAI API密钥
3. 运行以下命令：

```bash
mvn clean install
mvn spring-boot:run
```

4. 访问 http://localhost:8080

## API接口

### 发送消息
```
POST /api/chat/message
Content-Type: application/json

{
  "message": "我要订酒店",
  "sessionId": "session_123"
}
```

### 提交表单
```
POST /api/chat/form/submit
Content-Type: application/json

{
  "name": "jeffery",
  "destination": "北京",
  "formId": "form-id"
}
```

### 清除会话
```
DELETE /api/chat/session/{sessionId}
```

## 测试

运行所有测试用例：

```bash
mvn test
```

## 使用示例

1. 用户输入："我叫jeffery，打算明天去北京"
2. 系统回复："我注意到您有出行计划，请问您是想要预订酒店、购买机票，还是预订火车票呢？"
3. 用户输入："订酒店"
4. 系统生成酒店预订表单，包含姓名、目的地、入住日期等字段
5. 用户填写并提交表单

## 注意事项

- 需要有效的OpenAI API密钥
- 确保网络可以访问OpenAI API
- 表单数据提交后会在后端处理，实际项目中需要连接数据库保存

