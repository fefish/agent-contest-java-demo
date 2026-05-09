package com.contestdemo.runtime;

import java.util.LinkedHashMap;
import java.util.Map;

public final class McpTool {
    public interface Handler {
        Object call(Map<String, Object> args) throws Exception;
    }

    public final String name;
    public final String description;
    public final Map<String, Object> inputSchema;
    public final String kind;
    public final Handler handler;

    public McpTool(String name, String description, Map<String, Object> inputSchema, String kind, Handler handler) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
        this.kind = kind;
        this.handler = handler;
    }

    public Map<String, Object> toOpenAiTool() {
        Map<String, Object> function = new LinkedHashMap<>();
        function.put("name", name);
        function.put("description", description);
        function.put("parameters", inputSchema);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("type", "function");
        result.put("function", function);
        return result;
    }
}
