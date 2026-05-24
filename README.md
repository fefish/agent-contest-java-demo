# Agent 大赛 Java 最小 Demo

这是一个可以直接运行的 Agent 大赛最小 demo。赛方平台传入只含题面的运行题目 JSON，参赛系统接收题目并输出统一答案。

一句话能力说明：本 demo 提供“运行题目 JSON -> 主 Agent 编排 -> SKILL.md skill / MCP-style tool / sub-agent 调用 -> 统一结果输出”的最小闭环。

快速运行：

```bash
bash start.sh
```

赛方平台也可以传入自己的运行题目文件和结果输出路径：

```bash
bash start.sh <questions_json> <result_json>
```

用户自己测试时，可以直接修改 `source/examples/questions.json`，或把自己的题目文件作为第一个参数传入。

参赛者主要修改：

```text
source/src/main/java/com/contestdemo/solution/ContestantAgent.java    主 Agent
source/solution/skills/                                               Skill 包：SKILL.md 必需，skill.json/scripts 可选
source/src/main/java/com/contestdemo/toolkits/MainMcp.java            MCP-style tools 注册入口
source/solution/agents/                                               Sub-agent 包：AGENT.md + agent.json + scripts/Run.java
```

赛方主要维护：

```text
source/examples/questions.json    运行时题目文件，只含 id/question/files
```

结果输出：

```text
source/outputs/result.json
```

模型配置在当前仓库根目录的 `.env`。可以从样例复制：

```bash
cp .env.example .env
```

详细说明见 [doc/README.md](doc/README.md)。
