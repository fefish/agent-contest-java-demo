# 设计说明

## 角色边界

赛方负责维护含参考答案和评分字段的内部源任务 JSON，并在平台侧生成只含题面的运行题目 JSON。

参赛者负责开发主 Agent、自己的 SKILL.md skill 包、MCP-style tools、sub-agent 包，并在主 Agent 中实现编排策略。

## 运行链路

```text
questions.json
  -> start.sh
  -> com.contestdemo.Main
  -> BatchRunner
  -> 自动发现 source/solution/skills 和 source/solution/agents
  -> ContestantAgent.solve()
  -> context.callTool()
  -> LocalMcpClient
  -> built-in tools / skill runtime / sub-agents
  -> result.json
```

赛方内部源任务 JSON 可以包含 `reference_answer`、`expected_keywords`、`exclude_keywords`、`scoring_notes`、`score`、`difficulty`、`isPublic` 等私有字段。传给本 demo 的 `questions.json` 只保留 `id`、`question`、`files`。

## 文件权限

题目附件路径以 `questions.json` 所在目录为基准。运行器会把每题 `files` 解析为允许文件列表，`text_read_file` 只能读取这些声明过的文件。
