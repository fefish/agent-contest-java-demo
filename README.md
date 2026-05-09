# Agent 大赛 Java 最小 Demo

这是一个可以直接运行的 Agent 大赛 Java 最小 demo。赛方维护“题目 + 参考答案”的源 JSON，脚本会生成只含题目的公开 JSON；运行 demo 时，参赛系统只接收题目并输出答案。

一句话能力说明：本 demo 提供“赛方问答源 JSON -> 自动剥离答案生成题目 JSON -> 主 Agent 编排 -> SKILL.md skill / MCP-style tool / sub-agent 调用 -> 统一结果输出”的最小闭环。

快速运行：

```bash
bash bin/check_env.sh
bash bin/run_demo.sh
```

`run_demo.sh` 会先从 `source/examples/official_tasks.json` 生成 `source/examples/questions.json`，再执行题目并打印易读答案视图。

手动生成题目文件：

```bash
bash bin/prepare_questions.sh source/examples/official_tasks.json source/examples/questions.json
```

查看答案：

```bash
bash bin/show_answers.sh source/outputs/result.json
```

参赛者主要修改：

```text
source/src/main/java/com/contestdemo/solution/ContestantAgent.java   主 Agent
source/solution/skills/                                             Skill 包：SKILL.md 必需，skill.json/scripts 可选
source/solution/mcp/                                                MCP-style tools 说明与示例
source/solution/agents/                                             Sub-agent 包：AGENT.md + agent.json + scripts/Run.java
```

赛方主要维护：

```text
source/examples/official_tasks.json    赛方源任务，可包含 reference_answer/answer 等私有答案字段
source/examples/questions.json         运行时题目文件，只含 id/question/files
source/examples/files/                 题目附件，可选
```

模型配置在当前仓库根目录的 `.env`。可以从样例复制：

```bash
cp .env.example .env
```

本 demo 使用 Java 标准库实现，无需 Maven/Gradle；需要 JDK 11+。

详细说明见 [doc/README.md](doc/README.md)。
