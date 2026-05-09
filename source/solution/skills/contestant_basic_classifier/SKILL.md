---
name: contestant_basic_classifier
description: Assign a tiny demo label to text.
---

# contestant_basic_classifier

Use this skill when a task asks for a simple example label.

The executable reads:

```json
{"text": "..."}
```

It returns a JSON label such as `LONG` or `SHORT`.
