# 题目格式

赛方维护源任务 JSON，可以是 JSON list，也可以是包含 `tasks`、`questions` 或 `items` 的 JSON object。

源任务可以包含参考答案：

```json
{
  "id": "demo_002",
  "question": "请读取关联文件，并统计关键词“安全”“skill”“工具”分别出现了几次。",
  "files": ["files/simple_skill_note.txt"],
  "reference_answer": "安全出现 2 次，skill 出现 2 次，工具出现 1 次。"
}
```

生成运行题目：

```bash
bash bin/prepare_questions.sh source/examples/official_tasks.json source/examples/questions.json
```

运行题目只包含公开字段：

```json
{
  "id": "demo_002",
  "question": "请读取关联文件，并统计关键词“安全”“skill”“工具”分别出现了几次。",
  "files": ["files/simple_skill_note.txt"]
}
```

题目不声明参赛者 MCP-style tool 名、skill 名或 sub-agent 名。参赛系统自己发现和编排能力。
