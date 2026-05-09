package com.contestdemo.runtime;

import com.contestdemo.solution.ContestantAgent;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BatchRunner {
    private final LocalMcpClient mcp = new LocalMcpClient();

    public List<Map<String, Object>> runFile(Path questionPath, Path outputPath) throws Exception {
        List<Map<String, Object>> questions = QuestionIO.loadTasks(questionPath).stream()
                .map(QuestionIO::publicQuestion)
                .toList();
        Path questionDir = questionPath.toAbsolutePath().getParent();
        List<Map<String, Object>> results = new ArrayList<>();
        int index = 0;
        for (Map<String, Object> question : questions) {
            index++;
            String id = Json.string(question.getOrDefault("id", index));
            System.out.println("[" + index + "/" + questions.size() + "] running question " + id);
            Map<String, Object> result = runOne(question, questionDir);
            results.add(result);
            QuestionIO.writeJson(outputPath, results);
        }
        return results;
    }

    private Map<String, Object> runOne(Map<String, Object> question, Path questionDir) {
        TraceRecorder trace = new TraceRecorder();
        trace.add("question_start", "attempt", 1);
        try {
            AgentContext context = buildContext(question, questionDir, trace);
            trace.add("available_tools", "tools", mcp.toolNames());
            trace.add("available_skills", "skills", mcp.skillNames());
            Map<String, Object> answer = new ContestantAgent().solve(question, context);
            trace.add("question_end", "status", "success");
            Map<String, Object> result = baseResult(question, trace);
            result.put("status", "success");
            result.put("model_status", modelStatus(trace.events(), true));
            result.put("fallback_used", !result.get("model_status").equals("json_tools"));
            result.put("answer", answer.getOrDefault("answer", ""));
            result.put("confidence", answer.get("confidence"));
            result.put("reasoning_summary", answer.getOrDefault("reasoning_summary", ""));
            return result;
        } catch (Exception exc) {
            trace.add("question_end", "status", "error", "error", exc.getMessage());
            Map<String, Object> result = baseResult(question, trace);
            result.put("status", "error");
            result.put("model_status", "error");
            result.put("fallback_used", false);
            result.put("answer", "");
            result.put("confidence", 0);
            result.put("reasoning_summary", exc.getMessage());
            result.put("error", exc.getMessage());
            return result;
        }
    }

    private AgentContext buildContext(Map<String, Object> question, Path questionDir, TraceRecorder trace) {
        List<Path> allowed = new ArrayList<>();
        for (Object file : Json.asList(question.get("files"))) {
            allowed.add(questionDir.resolve(Json.string(file)).normalize().toAbsolutePath());
        }
        return new AgentContext(question, questionDir, allowed, mcp, trace);
    }

    private Map<String, Object> baseResult(Map<String, Object> question, TraceRecorder trace) {
        Map<String, Object> result = new LinkedHashMap<>(question);
        result.put("used_tools", trace.usedToolNames());
        result.put("used_skills", trace.usedSkillNames());
        result.put("trace", trace.events());
        return result;
    }

    private String modelStatus(List<Map<String, Object>> events, boolean success) {
        if (!success) {
            return "error";
        }
        boolean llmError = false;
        boolean jsonResponse = false;
        for (Map<String, Object> event : events) {
            String type = Json.string(event.get("type"));
            if (type.equals("llm_json_tool_fallback_error")) {
                llmError = true;
            }
            if (type.equals("llm_json_tool_response")) {
                jsonResponse = true;
            }
        }
        if (jsonResponse) {
            return "json_tools";
        }
        if (llmError) {
            return "local_fallback";
        }
        return "local_or_no_llm";
    }
}
