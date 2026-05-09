# 参赛者开发区

正式比赛通常只提交这个目录，以及主 Agent 文件：

```text
source/src/main/java/com/contestdemo/solution/ContestantAgent.java
source/solution/
```

题目对象由赛方传入，题目不会指定应该用哪个 MCP-style tool、skill 或 sub-agent。主 Agent 负责发现能力、选择能力、调用能力和组织最终答案。

## Skill 包

```text
skills/<skill_name>/SKILL.md
skills/<skill_name>/skill.json
skills/<skill_name>/scripts/Run.java
```

## MCP-style 工具层

Java 版示例 MCP-style tool 注册在：

```text
source/src/main/java/com/contestdemo/toolkits/MainMcp.java
```

## Sub-agent 包

```text
agents/<agent_name>/AGENT.md
agents/<agent_name>/agent.json
agents/<agent_name>/scripts/Run.java
```
