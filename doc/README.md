# Demo 使用说明

## 这份 Demo 是什么

这是一个“赛方出题、参赛者开发 Agent 系统、赛方收统一答案”的 Java 最小可运行 demo。

它只保留基础能力：

- 主 Agent：`source/src/main/java/com/contestdemo/solution/ContestantAgent.java`
- 参赛者 skill 包：`source/solution/skills/`
- 参赛者 MCP-style tools：由 `source/toolkits/MainMcp.java` 注册，参赛者可照示例扩展
- MCP-style 注册入口：`source/src/main/java/com/contestdemo/toolkits/MainMcp.java`
- sub-agent 包：`source/solution/agents/`
- 赛方源任务：`source/examples/official_tasks.json`
- 运行题目：`source/examples/questions.json`

本 demo 只负责从题目运行到答案，不包含赛方后续私有判断逻辑。

## 怎么跑

```bash
bash bin/check_env.sh
bash bin/run_demo.sh
```

输出在：

```text
source/outputs/result.json
```

`run_demo.sh` 做三件事：

1. 从 `source/examples/official_tasks.json` 生成只含题目的 `source/examples/questions.json`。
2. 编译并运行主 Agent，从题目生成答案。
3. 打印简洁答案视图。

如果只想验证框架，不走模型：

```bash
AGENT_DEMO_USE_LLM=0 bash bin/run_demo.sh
```

## 赛方题目文件

赛方可以维护一份含参考答案的源文件：

```json
{
  "id": "demo_001",
  "question": "请读取关联文件，并用一句话概括这份公开规则说明。",
  "files": ["files/public_policy.txt"],
  "reference_answer": "这里写赛方参考答案，运行时不会暴露给参赛 Agent。"
}
```

生成公开题目：

```bash
bash bin/prepare_questions.sh source/examples/official_tasks.json source/examples/questions.json
```

公开题目只保留 `id`、`question`、`files`。

## 参赛者开发什么

参赛者主要修改 `source/src/main/java/com/contestdemo/solution/ContestantAgent.java` 和 `source/solution/`。

主 Agent 通过下面这个接口调用 MCP-style tool、skill runtime 或 sub-agent：

```java
context.callTool("tool_name", args);
```

### 自定义 Skill

目录：

```text
source/solution/skills/<skill_name>/
```

每个 skill 是一个包：

```text
SKILL.md
skill.json        # 可选：声明 executable entrypoint 和 input_schema
scripts/Run.java  # 可选：skill_run 执行入口
references/       # 可选：skill_read_resource 可读取
assets/           # 可选：skill_read_resource 可读取
```

主 Agent 使用 skill 的通用工具：

```text
skill_load
skill_read_resource
skill_run
```

### MCP-style tools

Java 版 MCP-style tool 由 `source/src/main/java/com/contestdemo/toolkits/MainMcp.java` 注册。示例工具是 `contestant_mcp_echo`。

### Sub-agent

目录：

```text
source/solution/agents/<agent_name>/
```

每个 sub-agent 是一个包：

```text
AGENT.md
agent.json
scripts/Run.java
```

主 Agent 调 sub-agent 的方式：

```java
context.callTool("agent_delegate", Map.of(
    "agent_name", "verify_agent",
    "task", "请复核这个答案",
    "context_text", "..."
));
```

## 输出格式

运行后输出 JSON list：

```json
{
  "id": "demo_001",
  "question": "...",
  "status": "success",
  "model_status": "json_tools",
  "fallback_used": false,
  "answer": "...",
  "confidence": 0.95,
  "reasoning_summary": "...",
  "used_tools": ["text_read_file"],
  "used_skills": [],
  "trace": []
}
```

`trace`、`used_tools`、`used_skills` 用于调试和审计。
