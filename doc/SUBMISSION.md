# 提交说明

正式比赛建议只收参赛者的：

```text
source/src/main/java/com/contestdemo/solution/ContestantAgent.java
source/solution/
```

其中包括：

```text
skills/
mcp/
agents/
```

Skill 以 `SKILL.md` 作为必需入口；`skill.json` 和 `scripts/Run.java` 只在需要可执行 skill 时提供。MCP-style tools 可按 `contestant_mcp_echo` 示例扩展。Sub-agent 以 `AGENT.md`、`agent.json` 和 `scripts/Run.java` 作为一个可执行包。

本地调试：

```bash
bash bin/run_demo.sh
```

指定赛方题目运行：

```bash
bash bin/run_submit.sh path/to/questions.json path/to/result.json
```
