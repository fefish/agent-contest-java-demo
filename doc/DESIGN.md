# 设计说明

## 角色边界

赛方负责：

- 维护含参考答案的源任务 JSON。
- 用脚本生成只含题面的运行题目 JSON。
- 提供题目附件和统一运行器。
- 收集统一 `result.json`。

参赛者负责：

- 开发主 Agent。
- 开发自己的 SKILL.md skill 包。
- 开发自己的 MCP-style tools。
- 开发自己的 sub-agent 包。
- 在主 Agent 中实现编排策略。

## 运行链路

```text
official_tasks.json
  -> bin/prepare_questions.sh
  -> questions.json
  -> source/src/main/java/com/contestdemo/Main.java
  -> BatchRunner
  -> 自动发现 source/solution/skills、source/solution/mcp 和 source/solution/agents
  -> ContestantAgent.solve()
  -> AgentContext.callTool()
  -> LocalMcpClient
  -> built-in tools / skill runtime / sub-agents
  -> result.json
```

## 目录职责

```text
source/src/main/java/   Java 运行器和 baseline Agent
source/examples/        demo 源任务、运行题目和附件
source/solution/        参赛者开发区
```

## 能力边界

- `source/solution/skills/`：SKILL.md-first 能力包，适合有说明、资源和可选脚本的蒸馏 skill。
- `source/solution/mcp/`：MCP-style tool 说明和示例；Java 注册入口在 `MainMcp.java`。
- `source/solution/agents/`：可委托 sub-agent，适合单独流程、复核或多 agent 编排。

本 demo 只负责从题目运行到答案；赛方后续私有判断逻辑可以在独立系统中接入。
