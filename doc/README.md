# Demo 使用说明

## 这份 Demo 是什么

这是一个“赛方出题、参赛者开发 Agent 系统、赛方收统一答案”的 Java 最小可运行 demo。

它保留基础能力：

- 主 Agent：`source/src/main/java/com/contestdemo/solution/ContestantAgent.java`
- 参赛者 skill 包：`source/solution/skills/`
- 参赛者 MCP-style tools：`source/src/main/java/com/contestdemo/toolkits/MainMcp.java`
- sub-agent 包：`source/solution/agents/`
- 运行题目：`source/examples/questions.json`

本 demo 只负责从题目运行到答案，不包含赛方后续私有判断逻辑。

## 怎么跑

```bash
bash start.sh source/examples/questions.json source/outputs/result.json
```

输出在：

```text
source/outputs/result.json
```

`start.sh` 做两件事：

1. 运行主 Agent，从题目生成答案。
2. 打印简洁答案视图。

赛方平台也可以传入自己的运行题目文件和结果输出路径：

```bash
bash start.sh <questions_json> <result_json> [package_id]
```

第三个参数可选；传入后会作为 HTTP header `package_id` 透传给模型网关。

用户自己测试时，可以直接修改 `source/examples/questions.json`，或把自己的题目文件作为第一个参数传入，并把结果路径作为第二个参数。模型连通性、环境检查等调试能力建议放在赛方单独检查包里，不放进选手 demo 仓。

## 题目文件

选手仓运行时只接收题面文件：

```json
[
  {
    "id": "sample_001",
    "question": "这是公开占位样例题，请回答：OK"
  }
]
```

指定题目与输出：

```bash
bash start.sh path/to/questions.json path/to/result.json [package_id]
```

赛方内部可以维护含 `type`、`reference_answer`、`expected_keywords`、`score`、`difficulty`、`isPublic` 等字段的源任务 JSON；当前 `type` 固定为 `equal`。传给本 demo 的应是已经剥离私有字段后的运行题目 JSON。题目附件路径相对题目 JSON 所在目录。

## 参赛者开发什么

参赛者主要修改 `source/solution/` 和主 Agent。

主 Agent 入口：

```text
source/src/main/java/com/contestdemo/solution/ContestantAgent.java
```

主 Agent 通过 `context.callTool(...)` 调用 MCP-style tool、skill runtime 或 sub-agent。

Skill 包放在：

```text
source/solution/skills/<skill_name>/
```

Sub-agent 包放在：

```text
source/solution/agents/<agent_name>/
```

## 输出格式

运行后输出 JSON list。赛方收答案时主要读取：

```text
id
answer
confidence
status
```

`trace`、`used_tools`、`used_skills` 用于调试和审计。

## 模型配置

配置在当前仓库根目录的 `.env`。

```bash
cp .env.example .env
```
