---
name: contestant_keyword_count
description: Count exact keyword occurrences in a text payload.
---

# contestant_keyword_count

Use this skill when a task asks for counting exact keyword occurrences in text.

Input:

```json
{
  "text": "文本",
  "keywords": ["安全", "skill", "工具"]
}
```

The skill returns JSON with one `counts` object.
