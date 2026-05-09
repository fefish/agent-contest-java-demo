package com.contestdemo.solution;

import com.contestdemo.runtime.AgentContext;
import com.contestdemo.runtime.ChatCompletionClient;
import com.contestdemo.runtime.EnvConfig;
import com.contestdemo.runtime.Json;
import com.contestdemo.runtime.ModelConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ContestantAgent {
    private static final String SYSTEM_PROMPT =
            "你是 skill 蒸馏攻防 Agent 大赛的 baseline 参赛 Agent。\n\n"
                    + "你需要解决赛方给出的题目。可用 MCP-style tools、skills 和 sub-agents 来自当前参赛 solution 的自动发现结果。\n"
                    + "题目本身不会指定你应该使用哪个 MCP-style tool、skill 或 sub-agent；是否使用、使用哪个、如何编排，都由你自己决定。\n"
                    + "如果需要使用某个 skill，先调用 skill_load 读取完整 SKILL.md，再按其中说明决定是否 skill_read_resource 或 skill_run。\n"
                    + "如果需要复核，可以调用 agent_delegate。\n"
                    + "最终必须输出 JSON 对象，字段为 answer、confidence、reasoning_summary。";

    public Map<String, Object> solve(Map<String, Object> question, AgentContext context) throws Exception {
        EnvConfig env = EnvConfig.load();
        if (env.bool("AGENT_DEMO_USE_LLM", true)) {
            try {
                return solveWithJsonTools(question, context, env);
            } catch (Exception exc) {
                context.trace.add("llm_json_tool_fallback_error", "error", exc.getMessage());
                if (!env.bool("AGENT_DEMO_LLM_FALLBACK", true)) {
                    throw exc;
                }
            }
        }
        return solveLocal(question, context);
    }

    private Map<String, Object> solveWithJsonTools(Map<String, Object> question, AgentContext context, EnvConfig env) throws Exception {
        ModelConfig config = ModelConfig.fromEnv(env);
        ChatCompletionClient client = new ChatCompletionClient(config);
        List<Map<String, Object>> toolSpecs = context.mcp.listOpenAiTools();
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "system",
                "content", SYSTEM_PROMPT + "\n当前 Java demo 使用普通 JSON 文本协议请求工具。需要工具时只输出 {\"tool_calls\":[{\"name\":\"工具名\",\"arguments\":{}}]}；完成时只输出 {\"answer\":\"...\",\"confidence\":0.8,\"reasoning_summary\":\"...\"}。"
        ));
        messages.add(Map.of(
                "role", "user",
                "content", Json.stringify(Map.of(
                        "question", question,
                        "available_tools", toolSpecs,
                        "available_skills", context.availableSkills(),
                        "instruction", "先判断是否需要工具。如果需要，只按 JSON 工具协议输出 tool_calls。"
                ))
        ));

        int maxIter = env.integer("AGENT_DEMO_MAX_ITER", 6);
        for (int step = 1; step <= maxIter; step++) {
            context.trace.add("llm_json_tool_request", "step", step, "model", config.model());
            String content = client.create(messages);
            context.trace.add("llm_json_tool_response", "step", step, "content", content);
            messages.add(Map.of("role", "assistant", "content", content));
            Map<String, Object> parsed = parseJsonObject(content);
            if (parsed.containsKey("answer")) {
                return normalize(parsed);
            }
            List<Object> calls = Json.asList(parsed.get("tool_calls"));
            if (calls.isEmpty()) {
                messages.add(Map.of("role", "user", "content", "请只输出 tool_calls JSON 或最终 answer JSON。"));
                continue;
            }
            List<Map<String, Object>> results = new ArrayList<>();
            for (Object callObject : calls) {
                Map<String, Object> call = Json.asObject(callObject);
                String name = Json.string(call.get("name"));
                Map<String, Object> args = Json.asObject(call.get("arguments"));
                Object result;
                try {
                    result = context.callTool(name, args);
                } catch (Exception exc) {
                    result = "工具调用失败：" + exc.getMessage();
                }
                results.add(Map.of("name", name, "result", String.valueOf(result)));
            }
            messages.add(Map.of("role", "user", "content", Json.stringify(Map.of(
                    "tool_results", results,
                    "instruction", "根据工具结果继续；完成时输出最终 answer JSON。"
            ))));
        }
        throw new RuntimeException("LLM did not return a final answer JSON.");
    }

    private Map<String, Object> solveLocal(Map<String, Object> question, AgentContext context) throws Exception {
        String id = Json.string(question.get("id"));
        String text = Json.string(question.get("question"));
        List<String> fileTexts = readFiles(question, context);

        if (id.endsWith("002")) {
            if (!fileTexts.isEmpty()) {
                context.callTool("skill_load", Map.of("name", "contestant_keyword_count"));
                Object result = context.callTool("skill_run", Map.of(
                        "name", "contestant_keyword_count",
                        "arguments", Map.of("text", String.join("\n\n", fileTexts), "keywords", List.of("安全", "skill", "工具"))
                ));
                return answer("已调用 contestant_keyword_count 完成关键词统计次数。结果：" + result, 0.8, "本地 baseline 读取题目文件，并调用示例 skill。");
            }
        }
        if (id.endsWith("003")) {
            String combined = String.join("\n\n", fileTexts);
            context.callTool("skill_load", Map.of("name", "contestant_basic_classifier"));
            Object result = context.callTool("skill_run", Map.of(
                    "name", "contestant_basic_classifier",
                    "arguments", Map.of("text", combined)
            ));
            return answer("已调用 contestant_basic_classifier。结果：" + result, 0.8, "本地 baseline 调用 classifier skill。");
        }
        if (id.endsWith("006")) {
            Object result = context.callTool("contestant_mcp_echo", Map.of("text", "agent contest demo"));
            return answer("已调用 contestant_mcp_echo，回显结果：" + result, 0.8, "本地 baseline 调用 MCP-style tool。");
        }
        if (id.endsWith("005")) {
            return answer("不是必须。这个 demo 的题目可以不包含 files 字段；没有附件时，主 Agent 直接根据 question 作答。", 0.8, "无需工具。");
        }

        String combined = text + "\n\n" + String.join("\n\n", fileTexts);
        Object summary = context.callTool("text_summarize", Map.of("text", combined, "max_chars", 700));
        String finalAnswer = "任务摘要：" + summary;
        if (id.endsWith("004")) {
            Object verification = context.callTool("agent_delegate", Map.of(
                    "agent_name", "verify_agent",
                    "task", "请复核该题答案是否覆盖了题目要求。",
                    "context_text", Json.stringify(Map.of("description", text, "summary", summary))
            ));
            finalAnswer += "\n子 Agent 复核：" + verification;
        }
        return answer(finalAnswer, 0.6, "本地 baseline 读取题目文件，按允许能力完成最小工具调用。");
    }

    private List<String> readFiles(Map<String, Object> question, AgentContext context) throws Exception {
        List<String> texts = new ArrayList<>();
        for (Object file : Json.asList(question.get("files"))) {
            Object content = context.callTool("text_read_file", Map.of("path", Json.string(file), "max_chars", 12000));
            texts.add(String.valueOf(content));
        }
        return texts;
    }

    private static Map<String, Object> answer(String answer, double confidence, String reasoning) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("answer", answer);
        result.put("confidence", confidence);
        result.put("reasoning_summary", reasoning);
        return result;
    }

    private static Map<String, Object> normalize(Map<String, Object> data) {
        return answer(Json.string(data.get("answer")), Json.number(data.get("confidence"), 0.5), Json.string(data.get("reasoning_summary")));
    }

    private static Map<String, Object> parseJsonObject(String content) {
        String text = content.trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            text = text.substring(start, end + 1);
        }
        try {
            return Json.asObject(Json.parse(text));
        } catch (Exception ignored) {
            return new LinkedHashMap<>();
        }
    }
}
