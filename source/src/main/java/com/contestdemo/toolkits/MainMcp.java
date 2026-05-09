package com.contestdemo.toolkits;

import com.contestdemo.runtime.AgentRegistry;
import com.contestdemo.runtime.AgentContext;
import com.contestdemo.runtime.Json;
import com.contestdemo.runtime.McpTool;
import com.contestdemo.runtime.Schemas;
import com.contestdemo.runtime.SkillRuntime;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MainMcp {
    private final Map<String, McpTool> tools = new LinkedHashMap<>();
    private final SkillRuntime skills = new SkillRuntime();
    private final AgentRegistry agents = new AgentRegistry();

    public MainMcp() {
        registerBuiltIns();
        registerContestantTools();
    }

    public List<McpTool> tools() {
        return new ArrayList<>(tools.values());
    }

    public List<String> toolNames() {
        return tools.keySet().stream().filter(name -> !name.equals("agent_delegate")).toList();
    }

    public List<String> agentNames() {
        return agents.names();
    }

    public List<String> skillNames() {
        return skills.skillNames();
    }

    public List<Map<String, Object>> skillSummaries() {
        return skills.summaries();
    }

    public Object call(String name, Map<String, Object> args, AgentContext context) throws Exception {
        if (name.equals("agent_delegate")) {
            return agents.run(Json.string(args.get("agent_name")), Json.string(args.get("task")), Json.string(args.get("context_text")));
        }
        McpTool tool = tools.get(name);
        if (tool == null) {
            throw new IllegalArgumentException("unknown tool: " + name);
        }
        if (name.equals("text_read_file")) {
            args = new LinkedHashMap<>(args);
            args.put("path", resolveAllowedFile(Json.string(args.get("path")), context).toString());
        }
        return tool.handler.call(args);
    }

    private void registerBuiltIns() {
        register(new McpTool(
                "text_read_file",
                "Read an allowed UTF-8 text file and return its content.",
                Schemas.object(Map.of(
                        "path", Schemas.prop("string", "Path to an allowed question file."),
                        "max_chars", Schemas.prop("integer", "Maximum characters to return.")
                ), List.of("path")),
                "skill",
                args -> {
                    Path path = Path.of(Json.string(args.get("path")));
                    int max = (int) Json.number(args.get("max_chars"), 64000);
                    String text = Files.readString(path, StandardCharsets.UTF_8);
                    return text.length() > max ? text.substring(0, max) + "\n[truncated]" : text;
                }
        ));
        register(new McpTool(
                "text_summarize",
                "Create a short extractive summary from text.",
                Schemas.object(Map.of(
                        "text", Schemas.prop("string", "Text to summarize."),
                        "max_chars", Schemas.prop("integer", "Maximum characters in the summary.")
                ), List.of("text")),
                "skill",
                args -> {
                    String text = Json.string(args.get("text")).replaceAll("\\s+", " ").trim();
                    int max = (int) Json.number(args.get("max_chars"), 600);
                    return text.length() > max ? text.substring(0, max) : text;
                }
        ));
        register(new McpTool(
                "skill_load",
                "Load full SKILL.md instructions for a discovered skill package.",
                Schemas.object(Map.of(
                        "name", Schemas.prop("string", "Skill package name."),
                        "max_chars", Schemas.prop("integer", "Maximum characters of SKILL.md to return.")
                ), List.of("name")),
                "skill",
                args -> skills.loadSkill(Json.string(args.get("name")), (int) Json.number(args.get("max_chars"), 20000))
        ));
        register(new McpTool(
                "skill_read_resource",
                "Read a text resource bundled inside a skill package.",
                Schemas.object(Map.of(
                        "name", Schemas.prop("string", "Skill package name."),
                        "path", Schemas.prop("string", "Relative resource path under references/ or assets/."),
                        "max_chars", Schemas.prop("integer", "Maximum characters to return.")
                ), List.of("name", "path")),
                "skill",
                args -> skills.readResource(Json.string(args.get("name")), Json.string(args.get("path")), (int) Json.number(args.get("max_chars"), 12000))
        ));
        register(new McpTool(
                "skill_run",
                "Execute a skill package entrypoint script after reading its SKILL.md instructions.",
                Schemas.object(Map.of(
                        "name", Schemas.prop("string", "Skill package name."),
                        "arguments", Schemas.prop("object", "JSON arguments passed to the skill entrypoint.")
                ), List.of("name")),
                "skill",
                args -> skills.runSkill(Json.string(args.get("name")), Json.asObject(args.get("arguments")))
        ));
        register(new McpTool(
                "agent_delegate",
                "Delegate a bounded task to an allowed sub-agent.",
                Schemas.object(Map.of(
                        "agent_name", Schemas.prop("string", "Name of the configured sub-agent."),
                        "task", Schemas.prop("string", "Self-contained task for the sub-agent."),
                        "context_text", Schemas.prop("string", "Optional compact context for the sub-agent.")
                ), List.of("agent_name", "task")),
                "agent",
                args -> Json.stringify(Map.of("error", "agent_delegate is executed by runtime"))
        ));
    }

    private void registerContestantTools() {
        register(new McpTool(
                "contestant_mcp_echo",
                "Minimal contestant MCP-style tool example. Echoes the input text as JSON.",
                Schemas.object(Map.of("text", Schemas.prop("string", "Text to echo.")), List.of("text")),
                "mcp",
                args -> Json.stringify(Map.of("echo", Json.string(args.get("text"))))
        ));
    }

    private void register(McpTool tool) {
        tools.put(tool.name, tool);
    }

    private Path resolveAllowedFile(String rawPath, AgentContext context) {
        Path target = Path.of(rawPath);
        if (!target.isAbsolute()) {
            target = context.questionDir.resolve(target);
        }
        target = target.normalize().toAbsolutePath();
        for (Path allowed : context.allowedFilePaths) {
            if (target.equals(allowed.normalize().toAbsolutePath())) {
                return target;
            }
        }
        throw new IllegalArgumentException("File is not declared in the question: " + rawPath);
    }
}
