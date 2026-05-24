# 提交说明

正式比赛建议只收参赛者的：

```text
source/solution/
source/src/main/java/com/contestdemo/solution/
```

其中包括主 Agent、skill 包、MCP-style tools 和 sub-agent 包。

参赛者不应该修改：

```text
source/src/main/java/com/contestdemo/runtime/
source/examples/
```

本地调试：

```bash
bash start.sh
```

赛方平台指定运行题目和结果路径时，可以运行：

```bash
bash start.sh path/to/questions.json path/to/result.json
```
