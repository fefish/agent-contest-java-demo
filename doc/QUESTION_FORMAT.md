# 题目格式

题目 JSON 可以是 JSON list，也可以是包含 `tasks`、`questions` 或 `items` 的 JSON object。

## 运行题目 JSON

选手仓和赛方平台入口直接接收运行题目 JSON：

```bash
bash start.sh <questions_json> <result_json>
```

运行题目只保留公开字段：

```json
{
  "id": "sample_001",
  "question": "这是公开占位样例题，请回答：OK",
  "files": ["files/input.txt"]
}
```

公开字段：

- `id`
- `question`
- `files`，可选

`reference_answer`、`expected_keywords`、`exclude_keywords`、`scoring_notes`、`score`、`difficulty`、`isPublic` 不会发给参赛 Agent。赛方内部源任务需要在平台侧先转换成运行题目，再调用本 demo。

## 输出格式

统一输出格式由运行器固定：

```json
{
  "id": "sample_001",
  "question": "...",
  "status": "success",
  "model_status": "json_tools",
  "fallback_used": false,
  "answer": "...",
  "confidence": 0.8,
  "reasoning_summary": "...",
  "used_tools": [],
  "used_skills": [],
  "trace": []
}
```
